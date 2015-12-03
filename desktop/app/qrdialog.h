#ifndef QRDIALOG_H
#define QRDIALOG_H

#include <QDialog>

namespace Ui {
class QRDialog;
}

class AccountData;

class QRDialog : public QDialog
{
    Q_OBJECT

public:
    explicit QRDialog(QWidget *parent, AccountData* dataFrom, AccountData* dataTo, int xrpValue);
    ~QRDialog();    

private slots:
    void on_pushButton_clicked();

    void on_pushButton_3_clicked();

    void on_pushButton_2_clicked();

    void on_feeSpinBox_valueChanged(const QString &);

    void on_feeQRButton_clicked();

private:
    Ui::QRDialog *ui;
	AccountData* dataFrom;
    AccountData* dataTo;
    int xrpValue;
    void GenerateQR();
    void SetLabelTexts();
};

#endif // QRDIALOG_H
