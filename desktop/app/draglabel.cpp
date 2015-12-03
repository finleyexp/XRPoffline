#include "draglabel.h"
#include "data.h"
#include <QtWidgets>

QImage* GenerateImageFromData(AccountData* data, QFontMetrics &metric)
{
    bool canSend = data ? data->CanSend() : false;
    QString text = data->GetName();

	QSize size = metric.size(Qt::TextSingleLine, "00000..00000");
    QImage* imagePtr = new QImage(size.width() + 12, size.height()*2 + 5, QImage::Format_ARGB32_Premultiplied);
    QImage& image = *imagePtr;
    image.fill(qRgba(0, 0, 0, 0));

    QFont font;
    font.setStyleStrategy(QFont::ForceOutline);
    QLinearGradient gradient(0, 0, 0, image.height()-1);
    if (canSend)
    {
        gradient.setColorAt(0.0, QColor(241, 243, 184));
        gradient.setColorAt(0.97, QColor(241, 243, 184));
        gradient.setColorAt(1.0, QColor(200, 200, 127));
    }
    else
    {
        gradient.setColorAt(0.0, QColor(165, 207, 237));
        gradient.setColorAt(0.97, QColor(165, 207, 237));
        gradient.setColorAt(1.0, QColor(127, 127, 200));
    }

    QPainter painter;
    painter.begin(&image);
    painter.setRenderHint(QPainter::Antialiasing);
    painter.setBrush(gradient);
    painter.drawRoundedRect(QRectF(0, 0, image.width(), image.height()), 10, 10, Qt::RelativeSize);    

    painter.setFont(font);
    QPen penName(QColor("#000000"), 10, Qt::DotLine, Qt::FlatCap, Qt::RoundJoin);
    painter.setPen(penName);
    painter.drawText(QRect(QPoint(4,0), size), Qt::AlignLeft, text);

    QString addr = data->GetAddr();
    QString addrFull = addr.left(5) + ".." + addr.right(5);
    QPen penAddr(QColor("#808080"), 10, Qt::DotLine, Qt::FlatCap, Qt::RoundJoin);
    painter.setPen(penAddr);
    painter.drawText(QRect(QPoint(4, size.height()), size), Qt::AlignLeft, addrFull);

    painter.end();
    return imagePtr;
}

QImage* GenerateImageFromDataDonate(AccountData* data, QFontMetrics &metric)
{
    bool canSend = data ? data->CanSend() : false;
    QString text = data->GetName();

    QSize size = metric.size(Qt::TextSingleLine, "00000..00000");
    QImage* imagePtr = new QImage(size.width() + 12, size.height() + 3, QImage::Format_ARGB32_Premultiplied);
    QImage& image = *imagePtr;
    image.fill(qRgba(0, 0, 0, 0));

    QFont font;
    font.setStyleStrategy(QFont::ForceOutline);
    QLinearGradient gradient(0, 0, 0, image.height()-1);
    if (canSend)
    {
        gradient.setColorAt(0.0, QColor(241, 243, 184));
        gradient.setColorAt(0.97, QColor(241, 243, 184));
        gradient.setColorAt(1.0, QColor(200, 200, 127));
    }
    else
    {
        gradient.setColorAt(0.0, QColor(165, 207, 237));
        gradient.setColorAt(0.97, QColor(165, 207, 237));
        gradient.setColorAt(1.0, QColor(127, 127, 200));
    }

    QPainter painter;
    painter.begin(&image);
    painter.setRenderHint(QPainter::Antialiasing);
    painter.setBrush(gradient);
    painter.drawRoundedRect(QRectF(0, 0, image.width(), image.height()), 10, 10, Qt::RelativeSize);

    painter.setFont(font);
    QPen penName(QColor("#000000"), 10, Qt::DotLine, Qt::FlatCap, Qt::RoundJoin);
    painter.setPen(penName);
    painter.drawText(QRect(QPoint(4,0), size), Qt::AlignLeft, text);

    painter.end();
    return imagePtr;
}


DragLabel::DragLabel(int dataIndex, QWidget *parent)
	: QLabel(parent), dataIndex(dataIndex)
{
	AccountData* data = Wallet::GetWallet()->GetAccount(dataIndex);
    QFontMetrics metrics(font());
    QImage* img = GenerateImageFromData(data, metrics );
	setPixmap(QPixmap::fromImage(*img));
	delete img;
}
