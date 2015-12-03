#ifndef QRTEXTCONTENT_H
#define QRTEXTCONTENT_H

#include <QDialog>

namespace Ui {
class QRTextContent;
}

class QRTextContent : public QDialog
{
    Q_OBJECT

public:
    explicit QRTextContent(const QString& qrContent, const QString& windowTitle, QWidget *parent = 0);
    ~QRTextContent();

private:
    Ui::QRTextContent *ui;
};

#endif // QRTEXTCONTENT_H
