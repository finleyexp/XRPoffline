#-------------------------------------------------
#
# Project created by QtCreator 2015-01-07T16:30:34
#
#-------------------------------------------------

QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = XRPoffline
TEMPLATE = app

SOURCES += main.cpp\
        mainwindow.cpp \
    data.cpp \
    draglabel.cpp \
    dragwidget.cpp \
    qrdialog.cpp \
    addaccdialog.cpp \
    libqrencode/bitstream.c \
    libqrencode/mask.c \
    libqrencode/mmask.c \
    libqrencode/mqrspec.c \
    libqrencode/qrencode.c \
    libqrencode/qrinput.c \
    libqrencode/qrspec.c \
    libqrencode/rscode.c \
    libqrencode/split.c \
    qrWidget.cpp \
    qronlydialog.cpp \
    qrtextcontent.cpp

HEADERS  += mainwindow.h \
    data.h \
    draglabel.h \
    dragwidget.h \
    qrdialog.h \
    addaccdialog.h \
    qrWidget.h \
    qronlydialog.h \
    qrtextcontent.h

RESOURCES += offline.qrc

RC_FILE = app.rc

FORMS    += mainwindow.ui \
    addaccdialog.ui \
    qronlydialog.ui \
    qrdialog.ui \
    qrtextcontent.ui

win32: LIBS += -Ld:/libs/OpenSSL-Win32/lib -llibeay32

win32:CONFIG(release, debug|release): LIBS += -L$$OUT_PWD/../ripple/release/ -lripple
else:win32:CONFIG(debug, debug|release): LIBS += -L$$OUT_PWD/../ripple/debug/ -lripple
else:unix: LIBS += -L$$OUT_PWD/../ripple/ -lripple

INCLUDEPATH += $$PWD/../ripple
DEPENDPATH += $$PWD/../ripple

win32-g++:CONFIG(release, debug|release): PRE_TARGETDEPS += $$OUT_PWD/../ripple/release/libripple.a
else:win32-g++:CONFIG(debug, debug|release): PRE_TARGETDEPS += $$OUT_PWD/../ripple/debug/libripple.a
else:win32:!win32-g++:CONFIG(release, debug|release): PRE_TARGETDEPS += $$OUT_PWD/../ripple/release/ripple.lib
else:win32:!win32-g++:CONFIG(debug, debug|release): PRE_TARGETDEPS += $$OUT_PWD/../ripple/debug/ripple.lib
else:unix: PRE_TARGETDEPS += $$OUT_PWD/../ripple/libripple.a

unix: LIBS += -lcrypto
