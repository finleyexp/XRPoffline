#include "qrdialog.h"
#include "ui_qrdialog.h"
#include <ripple.h>
#include "data.h"
#include "mainwindow.h"
#include "qronlydialog.h"


QRDialog::QRDialog(QWidget *parent, AccountData* dataFrom, AccountData* dataTo, int xrpValue) :
    QDialog(parent), ui(new Ui::QRDialog), dataFrom(dataFrom), dataTo(dataTo), xrpValue(xrpValue)
{
    setWindowFlags(windowFlags() & (~Qt::WindowContextHelpButtonHint));

    ui->setupUi(this);

    QSizePolicy policy(QSizePolicy::Preferred, QSizePolicy::Preferred);
    policy.setHeightForWidth(true);
    ui->qr->setSizePolicy(policy);

    QStyle *style = QApplication::style();
    ui->pushButton->setIcon(style->standardIcon(QStyle::SP_DialogApplyButton, 0, this));
    ui->pushButton_2->setIcon(style->standardIcon(QStyle::SP_BrowserReload, 0, this));
    ui->pushButton_3->setIcon(style->standardIcon(QStyle::SP_DialogCancelButton, 0, this));


    SetLabelTexts();
    GenerateQR();
}



QRDialog::~QRDialog()
{
    delete ui;
}

void QRDialog::SetLabelTexts()
{
    QString formStr;
    formStr.sprintf("<b>%i XRP</b>", xrpValue);
    ui->amountLabel->setText( formStr);

    formStr.sprintf("<b>%s</b>, %s, sequence %i", qPrintable(dataFrom->GetName()), qPrintable(dataFrom->GetAddr()), dataFrom->GetSequence());
    ui->senderLabel->setText( formStr);

    formStr.sprintf("<b>%s</b>, %s", qPrintable(dataTo->GetName()), qPrintable(dataTo->GetAddr()));
    ui->recipientLabel->setText( formStr);
}

void QRDialog::on_pushButton_clicked()
{
    dataFrom->SetSequence(1 + dataFrom->GetSequence());
    ui->pushButton->setEnabled(false);
    ((MainWindow*)this->parent())->UpdateSequence();
    close();
}

void QRDialog::on_pushButton_3_clicked()
{
    close();
}

void QRDialog::on_pushButton_2_clicked()
{
    ui->qr->HideQR();
    QString addr = dataFrom->GetAddr();
    int seq = dataFrom->GetSequence();
    qrOnlyDialog dlg(this, qrOnlyDialog::qrAccountInfo, addr, &seq);
    if (dlg.exec()==QDialog::Accepted)
    {
        int val = dlg.GetInt();
        if (val!=0)
        {
            dataFrom->SetSequence(val);
            SetLabelTexts();
            GenerateQR();
        }
    }
    ui->qr->ShowQR();
}

void QRDialog::on_feeSpinBox_valueChanged(const QString & /*arg1*/)
{
   GenerateQR();
}

void QRDialog::GenerateQR()
{
    std::string hash;
    std::string command = rippleCommandSignedXRPPayment(
                dataFrom->GetAddr().toStdString(), dataFrom->GetKey().toStdString(), dataFrom->GetSequence(),
                dataTo->GetAddr().toStdString(), double(xrpValue), ui->feeSpinBox->value(), hash);

    ui->qr->setQRData(command.c_str());
    ui->qr->setToolTip("Transaction hash: " + QString(hash.c_str()));
}

void QRDialog::on_feeQRButton_clicked()
{
    ui->qr->HideQR();
    double fee = ui->feeSpinBox->value();
    qrOnlyDialog dlg(this, qrOnlyDialog::qrServerState, "", &fee);
    if (dlg.exec()==QDialog::Accepted)
    {
        double val = dlg.GetDouble();
        if (val!=0)
        {
            ui->feeSpinBox->setValue(val);
            GenerateQR();
        }
    }
    ui->qr->ShowQR();
}
