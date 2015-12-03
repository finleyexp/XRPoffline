#include "qrWidget.h"
#include <QPainter>
#include <QDebug>    
#include <QLineEdit>
#include "libqrencode/qrencode.h"
#include "qrtextcontent.h"

QRWidget::QRWidget(QWidget *parent) :
    hidden(false), QWidget(parent), data("")
{
}

void QRWidget::setQRData(const QString &data)
{
    this->data=data;
    update();
}

void QRWidget::paintEvent(QPaintEvent * /*pe*/)
{
    QPainter painter(this);

    if (data.isEmpty() || hidden)
    {
        QColor col("white");
        painter.setBrush(col);
        painter.drawRect(0,0,width(),height());

		QPen penAddr(QColor("#808080"), 10, Qt::DotLine, Qt::FlatCap, Qt::RoundJoin);
		painter.setPen(penAddr);
        if (data.isEmpty())
            painter.drawText(QRect(0,0, width(), height()), Qt::AlignCenter, "no data");
        return;
    }

    //NOTE: I have hardcoded some parameters here that would make more sense as variables.
    QRcode *qr = QRcode_encodeString(data.toStdString().c_str(), 1, QR_ECLEVEL_L, QR_MODE_8, 1);
    if(0!=qr){
        QColor fg("black");
        QColor bg("white");
        painter.setBrush(bg);
        painter.setPen(Qt::NoPen);
        painter.drawRect(0,0,width(),height());
        painter.setBrush(fg);
        const int s=qr->width>0?qr->width:1;
        const double w=width()-5;
        const double h=height()-5;
        const double aspect=w/h;
        const double scale=((2+(aspect>1.0?h:w)))/s;
        for(int y=0;y<s;y++){
            const int yy=y*s;
            for(int x=0;x<s;x++){
                const int xx=yy+x;
                const unsigned char b=qr->data[xx];
                if(b &0x01){
                    const double rx1=x*scale, ry1=y*scale;
                    QRectF r(rx1, ry1, scale, scale);
                    painter.drawRects(&r,1);
                }
            }
        }
        QRcode_free(qr);
    }
    else{
        QColor error("red");
        painter.setBrush(error);
        painter.drawRect(0,0,width(),height());
    }
    qr=0;
}

void QRWidget::mousePressEvent( QMouseEvent * /*event*/ )
{
    QRTextContent dlg(data, "QR Text",this);
    dlg.exec();
}
