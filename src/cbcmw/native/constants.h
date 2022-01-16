#ifndef CONSTANTS
#define CONSTANTS

#include <stdbool.h>

const char *MW_CLIENT_NAME = "Sedona-MW";
const bool MW_CLEAN_SESSION = true;

enum MqttConnectionState {
  STATUS_CONNECTING,
  STATUS_CONNACK_RECVD,
  STATUS_WAITING,
  STATUS_DISCONNECTING,
  STATUS_DISCONNECTED,
  STATUS_INTERNAL_ERROR,
  STATUS_NOHOPE
};

#define UNUSED(x) (void)(x)

#endif /* CONSTANTS */
