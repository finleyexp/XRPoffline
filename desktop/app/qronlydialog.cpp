#include "qronlydialog.h"
#include "ui_qronlydialog.h"
#include "qrWidget.h"
#include "qsizepolicy.h"
#include <ripple.h>
#include <QDoubleSpinBox>

qrOnlyDialog::qrOnlyDialog(QWidget *parent, qrMode mode, const QString& address, void* data) :
    QDialog(parent), ui(new Ui::qrOnlyDialog), mode(mode), doubleWidget(NULL), intWidget(NULL)
{
    setWindowFlags(windowFlags() & (~Qt::WindowContextHelpButtonHint));

    ui->setupUi(this);

    QString command;

    switch(mode)
    {
        case qrAccountInfo:
        {
            command = rippleCommandAddressInfo(address.toStdString()).c_str();
            ui->description->setText("This QR code contains command used to get informations about account, such as XRP balance or sequence number for next transaction.");
            this->setWindowTitle("Get account info");

            QLabel* label = new QLabel(this);
            label->setText("Result sequence:");
            QSpinBox* spinBox = new QSpinBox(this);
            spinBox->setMinimum(1);
            spinBox->setMaximum(9999999);
            spinBox->setValue(*(int*)data);
            QSizePolicy policy(QSizePolicy::Expanding, QSizePolicy::Preferred);
            spinBox->setSizePolicy(policy);
            QHBoxLayout* layout = new QHBoxLayout();
            layout->addWidget(label);
            layout->addWidget(spinBox);
            ui->verticalLayout->insertLayout(2, layout);
            intWidget = spinBox;

            break;
        }
        case qrServerState:
        {
            command = rippleCommandServerState().c_str();
            ui->description->setText("This QR code contains command used to get informations about server, usefull to determine current transaction fee.");
            this->setWindowTitle("Get transaction fee from server");


            QLabel* label = new QLabel(this);
            label->setText("Result fee:");
            QDoubleSpinBox* spinBox = new QDoubleSpinBox(this);
            spinBox->setMinimum(0.000001);
            spinBox->setMaximum(99.99000);
            spinBox->setSingleStep(0.001);
            spinBox->setDecimals(6);
            spinBox->setSuffix(" XRP");
            spinBox->setValue(*(double*)data);
            QSizePolicy policy(QSizePolicy::Expanding, QSizePolicy::Preferred);
            spinBox->setSizePolicy(policy);
            QHBoxLayout* layout = new QHBoxLayout();
            layout->addWidget(label);
            layout->addWidget(spinBox);
            ui->verticalLayout->insertLayout(2, layout);

            doubleWidget = spinBox;
            break;
        }
        case qrOther:
            ui->description->setText(*(QString*)data);
            this->setWindowTitle("QR");
            command = address;
            break;
    }

    ui->qrWidget->setQRData(command);
}

qrOnlyDialog::~qrOnlyDialog()
{
    delete ui;
}

double qrOnlyDialog::GetDouble()
{
    return doubleWidget? doubleWidget->value():0;
}

int qrOnlyDialog::GetInt()
{
    return intWidget? intWidget->value():0;
}

void qrOnlyDialog::on_buttonBox_accepted()
{

}

void qrOnlyDialog::on_buttonBox_rejected()
{

}
