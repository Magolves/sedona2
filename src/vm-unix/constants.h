#ifndef CONSTANTS
#define CONSTANTS

#include <stdbool.h>

extern const char *MW_CLIENT_NAME;
extern const bool MW_CLEAN_SESSION;

enum MqttConnectionState {
  STATUS_CONNECTING,
  STATUS_CONNACK_RECVD,
  STATUS_CONNECTED,
  STATUS_DISCONNECTING,
  STATUS_DISCONNECTED,
  STATUS_INTERNAL_ERROR,
  STATUS_DOWN
};

#define UNUSED(x) (void)(x)

#endif /* CONSTANTS */
