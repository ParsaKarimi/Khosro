#include <Arduino.h>
#include <WiFi.h>
#include <DNSServer.h>
#include <WebServer.h>
#include <FastLED.h>
#include <uri/UriBraces.h>
#include <uri/UriRegex.h>
#include "RTClib.h"
#include <AT24CX.h>
#include <Wire.h>

typedef unsigned char BYTE;

#define SSID "Khosro Productions"
#define PASSWORD "<Password>"
#define LED_PIN 2
#define SPEAKER_PIN 5
#define MICROPHONE_PIN 34
#define PUMP_PIN 19

#define NUM_LEDS 48
#define MAX_BRIGHTNESS 0xff
#define DATA_PIN 18

#define WATERING_DURATION 3500
#define WATERING_GAP 8 * 24 * 3600 * 1000UL
//                   ^ this number of days between each watering

DNSServer dnsServer;
IPAddress apIP(192, 168, 1, 128);
WebServer server(80);
RTC_DS1307 rtc;
AT24C32 time_keeper;

CRGB leds[NUM_LEDS];
struct {
    bool rtl;
    BYTE mode;
    BYTE n;
    unsigned int duration;
    CRGB color;
} color_config;
struct {
    bool rtl;
    BYTE mode;
    BYTE n;
    unsigned int duration;
} animation_config;

// put function declarations here:
void breathe(unsigned int duration);
void fill_from_left(unsigned int duration);
void fill_from_right(unsigned int duration);
void fill_from_both(unsigned int duration);
void fill_from_center(unsigned int duration);
void slide_over(BYTE n, unsigned int duration);
void wave_over(BYTE n, unsigned int duration);
void piece_by_piece(unsigned int duration, bool rtl);

void rainbow(int n);
void digital_wave(int n, unsigned int duration);
void portion(int n, unsigned int duration);
void color_window(int n, unsigned int duration, bool rtl);
void constant(CRGB color);

void heart_beat();
void speaker();
void get_speaker();
void led_mode();
void led_color();
void update_clock();
void get_clock();
void water();
void get_next_watering();

long last_watering;

bool speaker_state = false;

void setup() {
    // put your setup code here, to run once:
    Serial.begin(115200);
    pinMode(LED_PIN, OUTPUT);

    FastLED.setBrightness(MAX_BRIGHTNESS);
    FastLED.addLeds<WS2812, DATA_PIN, GRB>(leds, NUM_LEDS);
    pinMode(PUMP_PIN, OUTPUT);
    pinMode(SPEAKER_PIN, OUTPUT);
    pinMode(MICROPHONE_PIN, INPUT);
    analogReadResolution(12);

    digitalWrite(LED_PIN, HIGH);
    delay(200);
    digitalWrite(LED_PIN, LOW);

    WiFi.mode(WIFI_AP);
    WiFi.softAPConfig(apIP, apIP, IPAddress(255, 255, 255, 0));
    WiFi.softAP(SSID, PASSWORD);

    dnsServer.setTTL(300);
    dnsServer.start(53, "www.khosro.sampad.ir", apIP);

    IPAddress IP = WiFi.softAPIP();
    Serial.print("AP IP address: ");
    Serial.println(IP);

    server.on(UriRegex("^(?:|\\/)$"), HTTP_GET, heart_beat);
    server.on(UriRegex("^\\/speaker(?:|\\/)$"), HTTP_POST, speaker);
    server.on(UriRegex("^\\/speaker(?:|\\/)$"), HTTP_GET, get_speaker);
    server.on(UriRegex("^\\/led/color/((?:off|rainbow|dw|portion|cw|constant))(?:|\\/)$"), HTTP_POST, led_color);
    server.on(UriRegex("^\\/led/mode/((?:off|breathe|ffl|ffr|ffb|ffc|so|wo|pbp))(?:|\\/)$"), HTTP_POST, led_mode);
    server.on(UriRegex("^\\/clock(?:|\\/)$"), HTTP_POST, update_clock);
    server.on(UriRegex("^\\/clock(?:|\\/)$"), HTTP_GET, get_clock);
    server.on(UriRegex("^\\/water(?:|\\/)$"), HTTP_POST, water);
    server.on(UriRegex("^\\/water(?:|\\/)$"), HTTP_GET, get_next_watering);
    server.begin();

    if (!rtc.begin()) {
        Serial.println("Couldn't find RTC");
        digitalWrite(LED_PIN, HIGH);
        while (1);
    }
     
    if (!rtc.isrunning()) {
        Serial.println("RTC lost power, lets set the time!");
        rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
        time_keeper.writeLong(0x00, rtc.now().unixtime());
    } else {
        Serial.println(rtc.now().unixtime());
        Serial.println(time_keeper.readLong(0x00));
    }
    
    last_watering = (time_keeper.readLong(0x00) - rtc.now().unixtime()) * 1000;
    if (WATERING_GAP < (millis() - last_watering)) last_watering = -WATERING_GAP;
}

void loop() {
    // put your main code here, to run repeatedly:
    dnsServer.processNextRequest();
    server.handleClient();
    
    if ((millis() - last_watering) < WATERING_GAP) {
        digitalWrite(PUMP_PIN, LOW);
    } else if (WATERING_GAP <= (millis() - last_watering) && (millis() - last_watering) <= (WATERING_GAP + WATERING_DURATION)) {
        digitalWrite(PUMP_PIN, HIGH);
    } else if (WATERING_GAP + WATERING_DURATION < (millis() - last_watering)) {
        digitalWrite(PUMP_PIN, LOW);
        last_watering = millis();
        time_keeper.writeLong(0x00, rtc.now().unixtime());
    }
    
    switch (color_config.mode) {
        case 0:
            fill_solid(leds, NUM_LEDS, CRGB::Black);
            break;
        case 1:
            rainbow(color_config.n);
            break;
        case 2:
            digital_wave(color_config.n, color_config.duration);
            break;
        case 3:
            portion(color_config.n, color_config.duration);
            break;
        case 4:
            color_window(color_config.n, color_config.duration, color_config.rtl);
            break;
        case 5:
            constant(color_config.color);
            break;
        default:
            fill_solid(leds, NUM_LEDS, CRGB::Black);
            break;
    }
    
    switch (animation_config.mode) {
        case 0:
            break;
        case 1:
            breathe(animation_config.duration);
            break;
        case 2:
            fill_from_left(animation_config.duration);
            break;
        case 3:
            fill_from_right(animation_config.duration);
            break;
        case 4:
            fill_from_both(animation_config.duration);
            break;
        case 5:
            fill_from_center(animation_config.duration);
            break;
        case 6:
            slide_over(animation_config.n, animation_config.duration);
            break;
        case 7:
            wave_over(animation_config.n, animation_config.duration);
            break;
        case 8:
            piece_by_piece(animation_config.duration, animation_config.rtl);
            break;
        default:
            fill_solid(leds, NUM_LEDS, CRGB::Black);
            break;
    }

    FastLED.show();
}



// put function definitions here:
// ANIMATIONS

void breathe(unsigned int duration) {
    unsigned int frame = millis() % duration;
    float alpha = (cos(((double)frame / (double)duration) * PI * 2 + PI) + 1) / 2;

    BYTE brightness = alpha * MAX_BRIGHTNESS;
    FastLED.setBrightness(brightness);
}

void fill_from_left(unsigned int duration) {
    unsigned int frame = duration / (NUM_LEDS+2);
    BYTE filled = (millis() % duration) / frame;
    fill_solid(&leds[filled], NUM_LEDS - filled, CRGB::Black);
}
void fill_from_right(unsigned int duration) {
    unsigned int frame = duration / (NUM_LEDS+2);
    BYTE filled = (millis() % duration) / frame;
    fill_solid(leds, NUM_LEDS - filled, CRGB::Black);
}
void fill_from_both(unsigned int duration) {
    unsigned int frame = duration / (NUM_LEDS/2+2);
    BYTE filled = (millis() % duration) / frame;
    fill_solid(&leds[filled], NUM_LEDS -  2*filled, CRGB::Black);
}
void fill_from_center(unsigned int duration) {
    unsigned int frame = duration / (NUM_LEDS/2+2);
    BYTE filled = (millis() % duration) / frame;
    fill_solid(&leds[0], (NUM_LEDS/2) - filled, CRGB::Black);
    fill_solid(&leds[(NUM_LEDS/2) + filled], (NUM_LEDS/2) - filled, CRGB::Black);
}

void slide_over(BYTE n, unsigned int duration) {
    unsigned int frame = duration / (NUM_LEDS + n - 1);
    BYTE moved = (millis() % duration) / frame;

    fill_solid(leds, moved - n + 1, CRGB::Black);
    if (moved > NUM_LEDS) return;
    fill_solid(&leds[moved + 1], NUM_LEDS - moved + 1, CRGB::Black);
}

void wave_over(BYTE n, unsigned int duration) {
    unsigned int frame = duration / (NUM_LEDS + n - 1);
    BYTE moved = (millis() % duration) / frame;

    for (int i = 0; i < n && moved + i < NUM_LEDS; i++) {
        if (moved - n + i + 1 < 0) continue;

        float alpha = abs(cos((float)(i) / (n - 1) * PI));
        leds[moved - n + i + 1].fadeLightBy(alpha * 255);
    }

    fill_solid(leds, moved - n + 1, CRGB::Black);
    if (moved > NUM_LEDS) return;
    fill_solid(&leds[moved + 1], NUM_LEDS - moved + 1, CRGB::Black);
}

void piece_by_piece(unsigned int duration, bool rtl) {
    unsigned int index = (millis() % duration) / (duration / ((NUM_LEDS) * (NUM_LEDS+1) / 2));
    BYTE moved = 0;
    while (index >= NUM_LEDS-moved) {
        index -= NUM_LEDS-moved;
        moved += 1;
    }

    if (rtl) {
        fill_solid(&leds[NUM_LEDS-index], index, CRGB::Black);
        fill_solid(&leds[moved+1], NUM_LEDS-moved-index-2, CRGB::Black);
    } else {
        fill_solid(leds, index, CRGB::Black);
        fill_solid(&leds[index+1], NUM_LEDS-moved-index-1, CRGB::Black);
    }
}


// COLOR FUNCTIONS

void rainbow(int n) {
    fill_rainbow(leds, NUM_LEDS, 0, 256 / NUM_LEDS * n);
}

void digital_wave(int n, unsigned int duration) {
    fill_rainbow(leds, NUM_LEDS, (float)(millis() % duration) / duration * 255, 256 / NUM_LEDS);
}

void portion(int n, unsigned int duration) {
    unsigned int frame = duration / n;
    BYTE color_index = (millis() % duration) / frame;
    CHSV hsv_color = CHSV((float)color_index / n * 256, 255, 255);
    CRGB rgb_color;
    hsv2rgb_rainbow(hsv_color, rgb_color);
    constant(rgb_color);
}

void color_window(int n, unsigned int duration, bool rtl) {
    unsigned int frame = duration / n;
    BYTE color_index = (millis() % duration) / frame;

    // background
    CHSV hsv_color = CHSV((float)(color_index-1) / n * 256, 255, 255);
    CRGB rgb_color;
    hsv2rgb_rainbow(hsv_color, rgb_color);
    constant(rgb_color);

    // foreground
    hsv_color = CHSV((float)color_index / n * 256, 255, 255);
    rgb_color;
    hsv2rgb_rainbow(hsv_color, rgb_color);
    frame /= NUM_LEDS;
    BYTE filled = (millis() % (duration/n)) / frame;
    if (rtl) fill_solid(&leds[NUM_LEDS - filled], filled, rgb_color);
    else fill_solid(leds, filled, rgb_color);
}

void constant(CRGB color) {
    fill_solid(leds, NUM_LEDS, color);
}


// END POINTS
void heart_beat() {
    server.send(204, "", "");
}
void speaker() {
    speaker_state = !speaker_state;
    digitalWrite(SPEAKER_PIN, speaker_state);
    server.send(200, "application/json", "{ state: " + String(speaker_state?"true":"false") + " }");
}
void get_speaker() {
    server.send(200, "application/json", "{ state: " + String(speaker_state?"true":"false") + " }");
}
void led_mode() {
    String method = server.pathArg(0);
    if (method == "off") {
        FastLED.setBrightness(MAX_BRIGHTNESS);
        animation_config.mode = 0;
    } else if (method == "breathe") {
        animation_config.mode = 1;
        animation_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "ffl") {
        animation_config.mode = 2;
        animation_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "ffr") {
        animation_config.mode = 3;
        animation_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "ffb") {
        animation_config.mode = 4;
        animation_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "ffc") {
        animation_config.mode = 5;
        animation_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "so") {
        animation_config.mode = 6;
        animation_config.n = atoi(server.arg("n").c_str());
        animation_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "wo") {
        animation_config.mode = 7;
        animation_config.n = atoi(server.arg("n").c_str());
        animation_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "pbp") {
        animation_config.mode = 8;
        animation_config.duration = atoi(server.arg("duration").c_str());
        animation_config.rtl = server.arg("rtl") == "true";
    }
    server.send(204, "", "");
}
void led_color() {
    String method = server.pathArg(0);
    if (method == "off") {
        color_config.mode = 0;
    } else if (method == "rainbow") {
        color_config.mode = 1;
        color_config.n = atoi(server.arg("n").c_str());
    } else if (method == "dw") {
        color_config.mode = 2;
        color_config.n = atoi(server.arg("n").c_str());
        color_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "portion") {
        color_config.mode = 3;
        color_config.n = atoi(server.arg("n").c_str());
        color_config.duration = atoi(server.arg("duration").c_str());
    } else if (method == "cw") {
        color_config.mode = 4;
        color_config.n = atoi(server.arg("n").c_str());
        color_config.duration = atoi(server.arg("duration").c_str());
        color_config.rtl = server.arg("rtl") == "true";
    } else if (method == "constant") {
        color_config.mode = 5;
        color_config.color = CRGB(strtol(server.arg("color").c_str(), NULL, 16));
    }

    server.send(204, "", "");
}
void update_clock() {
    unsigned long dt = strtoul(server.arg("time").c_str(), NULL, 10) - rtc.now().unixtime();
    rtc.adjust(rtc.now() + TimeSpan(dt));
    time_keeper.writeLong(0x00, time_keeper.readLong(0x00) + dt);

    server.send(200, "text/plain", "{ time: " + String(rtc.now().unixtime()) + " }");
}
void get_clock() {
    server.send(200, "application/json", "{ time: " + String(rtc.now().unixtime()) + " }");
}
void water() {
    last_watering = millis();
    time_keeper.writeLong(0x00, rtc.now().unixtime());

    server.send(204, "", "");
}
void get_next_watering() {
    server.send(200, "application/json", "{ time: " + String(time_keeper.readLong(0x00) + (WATERING_GAP/1000)) + " }");
}