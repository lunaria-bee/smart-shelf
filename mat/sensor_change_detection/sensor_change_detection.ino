#include "HX711.h"
#include <stdlib.h>

//HX711 constructor (dout pin, sck pin)
HX711 load_cell[4]; //Load cell array

const int MAT_LENGTH = 24;            //Length of mat in inches
const int MAT_HEIGHT = 20;            //Height of mat in inches
const float WEIGHT_THRESHOLD = 1.5;    //Threshold to determine change in mat weight
const float CALIBRATION_WEIGHT = 11.5;  //How much weight to put on the mat for calibration

struct readings {
  float reading[4];
};

long t;               //Time
int scale_factor[4];  //Array of scale factors for calibration
int stable_timer; //Incrimenter for num of loops before stable is set to true
float current_weight; //Current weight on mat
readings previous, stable; //readings from previous loop, readings when weight becomes unstable
bool is_stable;

void setup() {
  int cal_inc = 0;
  
  Serial.begin(38400);
  
  //Initialize Load cell objects
  load_cell[0].begin(3, 2);  //Top left corner
  load_cell[1].begin(6, 5);  //Top right corner
  load_cell[2].begin(9, 8);  //Bottom right
  load_cell[3].begin(13, 12);  //Bottom left

  pinMode(A0, OUTPUT);
  pinMode(A1, OUTPUT);

  digitalWrite(A0, HIGH);
  digitalWrite(A1, HIGH);
  
  t=0;
  is_stable = true;

  //Serial.println("Taring");
  
  //Load cell tare
  for (int i = 0; i <4; i++)  { //Initialize scale factor and zero scale
      
      scale_factor[i] = 1000;
      load_cell[i].set_scale(scale_factor[i]);
      load_cell[i].tare(10);
      load_cell[i].tare(20);
      previous.reading[i] = 0;
      stable.reading[i] = 0;
    }

  //Serial.println("Done taring, Calibrating");

  //Calibration
  double temp_reading = 0;
    
    while (load_cell[0].get_units(5) < WEIGHT_THRESHOLD) {  //Wait for weight to be put on mat
      digitalWrite(A0, LOW);
      delay(250);
      digitalWrite(A0, HIGH); }

    //Serial.println("Weight detected.");
      
    while (cal_inc < 10) {  //wait for sensor readings to stabilize
      if (abs(load_cell[0].get_units(5) - temp_reading) < WEIGHT_THRESHOLD)
        cal_inc++;
      delay(50);
      temp_reading = load_cell[0].get_units(5);
    }

    //Serial.println("Stable");
    
    for (int i = 0; i <4; i++)  {   //Run calibration loop for all sensors
      temp_reading = load_cell[i].get_units(5);
      cal_inc = 0;
      while (cal_inc < 5) //Loop until reading is close enough to actual value
      {
        temp_reading = load_cell[i].get_units(5);
        
        scale_factor[i] *= 4*temp_reading/CALIBRATION_WEIGHT;
        
        load_cell[i].set_scale(scale_factor[i]);

        if (abs(temp_reading - CALIBRATION_WEIGHT/4) < WEIGHT_THRESHOLD/4) {
          cal_inc++;
        }
       
      }      
    }

    //Serial.println("Calibration done, remove weight from mat");

    
    
    while (abs(load_cell[0].get_units(3)) > WEIGHT_THRESHOLD/4) {
      digitalWrite(A0, LOW);
      delay(250);
      digitalWrite(A0, HIGH);
    }

    digitalWrite(A0, LOW);
    
    for (int i = 0; i < 4; i++) {
      load_cell[i].tare(20);
    }

    //Serial.println("Starting scale");
   
}

void loop() {
  readings current, delta; //Current sensor readings and change in readings
  float stable_difference, recent_difference; //the difference in sensor readings since last period of stability and the difference between the current and last loop
  current_weight = 0;
  recent_difference = 0;
  stable_difference = 0;

  if (millis()-250 > t) {
    //Serial.print("Current readings: ");
    for (int i = 0; i < 4; i++) { //Loop to get sensor readings and put them into readings array
      current.reading[i] = load_cell[i].get_units(3); //Take reading
      current_weight+=current.reading[i]; //Sum all readings to get total weight
      delta.reading[i] = current.reading[i] - previous.reading[i];  //Find change in readings from previous reading and current
      recent_difference+=delta.reading[i];  //Sum all changes in sensor readings to find total change in weight
      stable_difference += current.reading[i] - stable.reading[i];  //Find how much the sensor reading has changed since the mat was last stable
      previous.reading[i] = current.reading[i]; //Set current reading to previous reading for the next reading
    }
    
    if (is_stable) {                                      //Checks if board is stable and changes accordingly
      if (abs(stable_difference) > WEIGHT_THRESHOLD) {    //Stable to unstable
        stable_timer++;
        if (stable_timer > 4) {
          is_stable = false;
          stable_timer = 0;
        }
      }
    }
    else {
      digitalWrite(A0, HIGH);
      delay(50);
      digitalWrite(A0, LOW);
      if (abs(recent_difference) < WEIGHT_THRESHOLD) {    //Unstable to stable
        stable_timer++;
      }
      if (stable_timer > 4){
        is_stable = true;
        stable_timer = 0;
        for (int i = 0; i < 3; i++) {
          Serial.print((current.reading[i] - stable.reading[i])/10);
          Serial.print(",");
        }
        Serial.println((current.reading[3] - stable.reading[3])/10);
        stable = current;
      }
    }
  t = millis();
  }
}
