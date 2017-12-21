#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <ESP8266HTTPClient.h>
#include <U8g2lib.h>
#include <ArduinoJson.h>
#define FASTLED_ESP8266_RAW_PIN_ORDER
#include "FastLED.h"

FASTLED_USING_NAMESPACE

const char* ssid = "Villa Straylight";
const char* password = "his teeth a webwork of East European steel and brown decay.";

#define MAX_REGIONS 8
#define MAX_DYNAMICS 32

#define DATA_PIN  4
#define CLOCK_PIN 5
#define LED_TYPE    WS2812B
#define COLOR_ORDER GRB
#define NUM_LEDS    60

CRGB leds[NUM_LEDS];

#define BRIGHTNESS          32
#define FRAMES_PER_SECOND  60

ESP8266WebServer server(80);

U8X8_SSD1306_128X64_NONAME_4W_SW_SPI u8x8(/*clock*/ D13, /*data*/ D11, /*cs*/ D10, /*dc*/ D9 , /*reset*/ D8);

class Byte {
  public:
    uint8_t v;

    Byte() {
      v = 0;
    }

    Byte(uint8_t val) {
      v = val;
    }

    virtual void update() {}

    static Byte *parseJSON(JsonVariant json);
};


class Dynamic : public Byte {
  uint8_t mode;   // 0 = cycle from 0 to 255 then reset to 0, 1 = cycle from 0 to 255 then back to 0
  int8_t step = 1; // current only supports 1 or -1

  public:
    void update() {
      v += step;
      if (mode == 1) {
        if ((step == 1 && v == 255) || (step == -1 && v == 0)) {
          step = -step;
        }
      }
    }

    static Byte *parseJSON(JsonObject& json);
};

Dynamic* dynamics[MAX_DYNAMICS];

Byte * Byte::parseJSON(JsonVariant json) {
  if (json.is<JsonObject&>()) {
    return Dynamic::parseJSON(json.as<JsonObject&>());
  } else {
    return new Byte(json.as<uint8_t>());
  }
}

Byte * Dynamic::parseJSON(JsonObject& json) {
  int idx = json["id"].as<int>();
  if (idx < 0 || idx >= MAX_DYNAMICS) return 0;
  if (!dynamics[idx]) {
    Dynamic *d = new Dynamic();
    dynamics[idx] = d;
  }

  if (json["mode"].is<uint8_t>())
    dynamics[idx]->mode = json["mode"].as<uint8_t>();

  if (json["value"].is<uint8_t>())
    dynamics[idx]->v = json["value"].as<uint8_t>();
  
  return dynamics[idx];
}


class Pattern {
  public:
    virtual void drawFrame(int start, int count);
    virtual String toString();
    static Pattern *parseJSON(JsonObject& json);
};

class SolidRGB: public Pattern {
  public:
    Byte *red;
    Byte *blue;
    Byte *green;

    SolidRGB(uint8_t r, uint8_t g, uint8_t b) {
      red = new Byte(r);
      blue = new Byte(b);
      green = new Byte(g);
    }

    SolidRGB(Byte *r, Byte *g, Byte *b) {
      red = r;
      blue = b;
      green = g;
    }

    void drawFrame(int start, int count) {
      fill_solid( &(leds[start]), count, CRGB(red->v, green->v, blue->v));
    }

    String toString() {
      String s = "SolidRGB(r=";
      s += red->v;
      s += ", b=";
      s += blue->v;
      s += ", g=";
      s += green->v;
      s += ")";
      return s;
    }

    static Pattern *parseJSON(JsonObject& json) {
      Byte *r = Byte::parseJSON(json["red"]);
      Byte *b = Byte::parseJSON(json["blue"]);
      Byte *g = Byte::parseJSON(json["green"]);
      return new SolidRGB(r, g, b);
    }
};

class SolidHSV: public Pattern {
  private:
    SolidHSV(uint8_t h, uint8_t s, uint8_t v) {
      hue = new Byte(h);
      saturation = new Byte(s);
      value = new Byte(v);
    }

    SolidHSV(Byte *h, Byte *s, Byte *v) {
      hue = h;
      saturation = s;
      value = v;
    }

  public:
    Byte *hue;
    Byte *saturation;
    Byte *value;

    void drawFrame(int start, int count) {
    //  int end = region->start + region->count;
    //  for (int i = region->start; i < end; i++) {
    //    leds[i].setHSV(hue, saturation, value);
    //  }
      fill_solid( &(leds[start]), count, CHSV(hue->v, saturation->v, value->v));
    }

    String toString() {
      String s = "SolidHSV(h=";
      s += hue->v;
      s += ", s=";
      s += saturation->v;
      s += ", v=";
      s += value->v;
      s += ")";
      return s;
    }

    static Pattern *parseJSON(JsonObject& json) {
      Byte *h = Byte::parseJSON(json["hue"]);
      Byte *s = Byte::parseJSON(json["saturation"]);
      Byte *v = Byte::parseJSON(json["value"]);
      return new SolidHSV(h, s, v);
    }
};

class Rainbow: public Pattern {
  public:
    Byte *start_hue;
    int delta = 5;

    Rainbow(uint8_t h) {
      start_hue = new Byte(h);
    }

    Rainbow(Byte *h) {
      start_hue = h;
    }

    void drawFrame(int start, int count) {
      fill_rainbow( &(leds[start]), count, start_hue->v, delta );
    }

    String toString() {
      String s = "Rainbow(h=";
      s += start_hue->v;
      s += ", d=";
      s += delta;
      s += ")";
      return s;
    }

    static Pattern *parseJSON(JsonObject& json) {
      Byte *h = Byte::parseJSON(json["start_hue"]);
      Rainbow *p = new Rainbow(h);
      if (json["delta"].is<uint8_t>()) {
        p->delta = json["delta"].as<uint8_t>();
      }
      return p;
    }
};

class Region {
  public:
    bool enabled;
    int start;
    int count;
    Pattern *pattern;

    Region(int s, int c, bool e, Pattern *p) {
      start = s;
      count = c;
      pattern = p;
      enabled = e;
    }

    static Region *parseJSON(JsonObject& json) {
      int start = json["start"].as<int>();
      int count = json["count"].as<int>();
      bool enabled = json["enabled"].as<bool>();
      Pattern *p = Pattern::parseJSON(json["pattern"].as<JsonObject&>());
      return new Region(start, count, enabled, p);
    }
};

Pattern * Pattern::parseJSON(JsonObject& json) {
  String type = json["type"].as<String>();
  if (type.compareTo(String("solid_rgb")) == 0) {
    return SolidRGB::parseJSON(json);
  } else if (type.compareTo(String("solid_hsv")) == 0) {
    return SolidHSV::parseJSON(json);
  } else if (type.compareTo(String("rainbow")) == 0) {
    return Rainbow::parseJSON(json);
  } else {
    u8x8.print("Unknown type: '");
    u8x8.print(type);
    u8x8.print("'\n");
    return 0;
  }
}


Region* regions[MAX_REGIONS];

void debug(char * msg) {
  u8x8.clear();
  u8x8.print( WiFi.localIP().toString().c_str() );
  u8x8.print("\n");
  u8x8.print(msg);
  u8x8.print(":\n");
  u8x8.print((server.method() == HTTP_GET)?"GET\n":"POST\n");
  String uri = server.uri();
  for (int i = 0; i < uri.length(); i += 16) {
    u8x8.print(uri.substring(i, i+16).c_str());
    u8x8.print("\n");
  }
}

void handleConfig() {
  debug("OK");
  if (server.hasArg("plain") == false) {
    server.send(200, "text/plain", "Body not received");
    return;
  }

  DynamicJsonBuffer json(2000);
  JsonArray& root = json.parseArray(server.arg("plain"));
  if (!root.success()) {
    server.send(200, "text/plain", "Failed to parse JSON:\n"+server.arg("plain"));
    return;
  }
  String message;
//  root.prettyPrintTo(message);
//  message = "Body:\n" + message;
//  message += "\n";

  for (int i = 0; i < 8; i++) {
    if (i >= root.size()) {
      regions[i] = 0;
    } else {
      regions[i] = Region::parseJSON(root[i].as<JsonObject&>());
        message += "Region ";
        message += i;
        if (regions[i] && regions[i]->pattern) {
          message += " ok: ";
          message += regions[i]->pattern->toString();
        } else {
          message += " not configured";
        }
        message += "\n";
    }
  }
  server.send(200, "text/plain", message);
}

void handleRoot() {
  debug("OK");
  String message = "LEDController up ";
  message += (millis() / 1000);
  message += " s";
  server.send(200, "text/plain", message);
}

void handleNotFound(){
  debug("Not found");
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET)?"GET":"POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";
  for (uint8_t i=0; i<server.args(); i++){
    message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
  }
  server.send(404, "text/plain", message);
}

void setup(void){
  WiFi.begin(ssid, password);
  u8x8.begin();
  u8x8.setFont(u8x8_font_victoriabold8_r);
  int x = 0;

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    u8x8.drawString(x, 0, ".");
    x++;
  }
  u8x8.clear();
  u8x8.print("Connected to\n");
  u8x8.print(ssid);
  u8x8.print("\n");
  u8x8.print( WiFi.localIP().toString().c_str() );
  u8x8.print("\n");

  if (MDNS.begin("esp8266")) {
    u8x8.print("MDNS responder\nstarted\n");
  }
  
  server.on("/", handleRoot);

  server.on("/inline", [](){
    server.send(200, "text/plain", "this works as well");
  });

  server.on("/config", HTTP_POST, handleConfig);

  server.onNotFound(handleNotFound);

  server.begin();
  u8x8.print("HTTP server up");


  // notify armitage of our address
  HTTPClient http;
  String query = "http://192.168.1.9/assistantdm/leds/";
  query += WiFi.localIP().toString();
  http.begin(query);
  int httpCode = http.GET();
  if (httpCode != HTTP_CODE_OK) {
    // do what?
    u8x8.print(http.errorToString(httpCode).c_str());
  }

  FastLED.addLeds<LED_TYPE,DATA_PIN,COLOR_ORDER>(leds, NUM_LEDS).setCorrection(TypicalLEDStrip);
//  FastLED.addLeds<APA102, DATA_PIN, CLOCK_PIN, BGR, DATA_RATE_MHZ(24)>(leds, NUM_LEDS).setCorrection(TypicalLEDStrip);
 
  // set master brightness control
  FastLED.setBrightness(BRIGHTNESS);

  for (int i = 0; i < MAX_DYNAMICS; i++)
    dynamics[i] = 0;
  for (int i = 0; i < MAX_REGIONS; i++)
    regions[i] = 0;

  regions[0] = new Region(0, 60, true, new Rainbow((uint8_t)0));
//  u8x8.clear();
}


void loop()
{
  FastLED.clear();
  
  for (int i = 0; i < MAX_REGIONS; i++) {
    if (regions[i] && regions[i]->enabled) {
      regions[i]->pattern->drawFrame(regions[i]->start, regions[i]->count);
    }
  }

  FastLED.show();

  for (int i = 0; i < MAX_DYNAMICS; i++) {
    if (dynamics[i])
      dynamics[i]->update();
  }

//  u8x8.home();
//  for (int i = 0; i < 4; i++) {
//    if (dynamics[i]) {
//      u8x8.print(i);
//      u8x8.print(": ");
//      u8x8.print(dynamics[i]->v);
//      u8x8.print("      \n");
//    }
//  }
  
  FastLED.delay(1000/FRAMES_PER_SECOND); 

  server.handleClient();
}

