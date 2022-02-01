add_sedona_kit(mqtt-mosquitto)

add_test(NAME sedona-mqtt COMMAND svm-mqtt ${CMAKE_SOURCE_DIR}/scode/test-mqtt.scode -test) 
