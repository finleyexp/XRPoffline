#ifndef STDAFX_H
#define STDAFX_H

#include <cstddef>
#include <climits>
#include <string.h>
#include <exception>
#include <stdexcept>
#include <openssl/rand.h>
#include <openssl/ripemd.h>
#include <openssl/pem.h>

#include <openssl/evp.h>
#include <openssl/aes.h>

#include <algorithm>
#include <cassert>
#include <sstream> 
#include <stdint.h>
#include <string>
#include <vector>

#ifndef nullptr
#define nullptr 0
#endif

#ifndef WIN32

#include <stdint.h>
typedef int64_t ULONGLONG;
typedef unsigned char BYTE;
typedef  int32_t DWORD;
#endif

#endif
