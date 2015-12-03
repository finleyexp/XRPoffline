#ifndef ADDACCDIALOG_H
#define ADDACCDIALOG_H

#include <QDialog>

namespace Ui {
class AddAccDialog;
}

class AccountData;

class AddAccDialog : public QDialog
{
    Q_OBJECT

public:

    enum Mode {
        edit,
        newGenerate,
        newSender,
        newRecipient
    };

    explicit AddAccDialog(AccountData* accForEdit, Mode mode, QWidget *parent = 0);
    ~AddAccDialog();
	AccountData* GetResult() { return result; }

private slots:
    void on_buttonBox_accepted();

    void on_buttonBox_rejected();

    void on_secretKey_textChanged(const QString &arg1);

    void on_address_textChanged(const QString &arg1);

    void secretFocusChanged(bool receivedFocus);

    void on_infoQR_clicked();

    void on_seqHelp_clicked();

    void on_displayName_textChanged(const QString &arg1);

    void on_accountQR_clicked();

private:

    void generateAddr();

    void UpdateStatus();

    Ui::AddAccDialog *ui;
	
	AccountData* srcData;
	AccountData* result;
    Mode mode;
};

#endif // ADDACCDIALOG_H
