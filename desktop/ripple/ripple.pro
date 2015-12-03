#-------------------------------------------------
#
# Project created by QtCreator 2015-01-11T09:30:36
#
#-------------------------------------------------

QT       -= core gui

TARGET = ripple
TEMPLATE = lib
CONFIG += staticlib \
	  precompile_header

PRECOMPILED_HEADER = stdafx.h

INCLUDEPATH += d:/libs/OpenSSL-Win32/include

SOURCES += ripple.cpp \
	   sign.cpp \
	   aesCrypter.cpp

HEADERS += stdafx.h \
	   ripple.h

win32-msvc* {
PRECOMPILED_SOURCE = stdafx.cpp
}
