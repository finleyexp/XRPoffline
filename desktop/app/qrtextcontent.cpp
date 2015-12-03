#include "qrtextcontent.h"
#include "ui_qrtextcontent.h"

QRTextContent::QRTextContent(const QString& qrContent, const QString& windowTitle, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::QRTextContent)
{
    setWindowFlags(windowFlags() & (~Qt::WindowContextHelpButtonHint));

    ui->setupUi(this);
    ui->content->setText(qrContent);
    setWindowTitle(windowTitle);
}

QRTextContent::~QRTextContent()
{
    delete ui;
}
