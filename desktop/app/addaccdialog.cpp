#include "addaccdialog.h"
#include "ui_addaccdialog.h"
#include "data.h"
#include "mainwindow.h"
#include <ripple.h>
#include "qronlydialog.h"
#include "qrtextcontent.h"

extern QString sequenceHelp;



AddAccDialog::AddAccDialog(AccountData* accForEdit, Mode mode, QWidget *parent) :
	QDialog(parent), ui(new Ui::AddAccDialog), srcData(NULL), mode(mode), result(NULL)
{    
    setWindowFlags(windowFlags() & (~Qt::WindowContextHelpButtonHint));

    ui->setupUi(this);

    ui->infoQR->setEnabled(false);
    ui->accountQR->setEnabled(false);	
    QStyle *style = QApplication::style();
    ui->seqHelp->setIcon(style->standardIcon(QStyle::SP_MessageBoxQuestion, 0, this));

    switch(mode)
    {
        case edit:
            ui->status->setHidden(true);
            srcData = accForEdit;
            if (srcData)
            {
                this->setWindowTitle("Edit Account");
                ui->displayName->setText(srcData->GetName());
                ui->address->setText(srcData->GetAddr());
                ui->address->setReadOnly(true);
                ui->sequence->setValue(srcData->GetSequence());                

                if (srcData->GetKey().isEmpty())
                {
                    ui->labelSequence->setHidden(true);
                    ui->seqHelp->setHidden(true);
                    ui->sequence->setHidden(true);
                    ui->infoQR->setHidden(true);
                    ui->secretKey->setHidden(true);
                    ui->labelSecret->setHidden(true);
                }
                else
                {
                    ui->secretKey->setEchoMode(QLineEdit::PasswordEchoOnEdit);
                    ui->secretKey->setText(srcData->GetKey());
                    ui->secretKey->setReadOnly(true);
                }
            }
            break;
        case newGenerate:
            this->setWindowTitle("Generate new account");
            generateAddr();
            ui->labelSequence->setHidden(true);
            ui->seqHelp->setHidden(true);
            ui->sequence->setHidden(true);
            ui->infoQR->setHidden(true);
            ui->status->setText("New generated account becomes active once reserve amount of XRP is sent to generated address. WRITE DOWN THE SECRET KEY to prevent loosing access to this account!");
            ui->secretKey->setReadOnly(true);
            ui->address->setReadOnly(true);
            break;
        case newSender:
            this->setWindowTitle("Add Sender/Recipient account");
            ui->address->setEnabled(false);
            ui->address->setToolTip("Account address is generated from secret key.");
            break;
        case newRecipient:
            this->setWindowTitle("Add Recipient account");
            ui->secretKey->setHidden(true);
            ui->labelSecret->setHidden(true);
            ui->accountQR->setHidden(true);

            ui->labelSequence->setHidden(true);
            ui->seqHelp->setHidden(true);
            ui->sequence->setHidden(true);
            ui->infoQR->setHidden(true);            

            break;
    }    
    UpdateStatus();
    this->window()->resize(0,0);
}

AddAccDialog::~AddAccDialog()
{
    delete ui;
	if (result)
		delete result;
}

void AddAccDialog::secretFocusChanged(bool receivedFocus)
{
    if (mode!=edit) return;
    if (receivedFocus)
        ui->secretKey->setEchoMode(QLineEdit::Normal);
    else
        ui->secretKey->setEchoMode(QLineEdit::PasswordEchoOnEdit);

}

void AddAccDialog::on_buttonBox_accepted()
{
	if (srcData)
	{
        srcData->SetName(ui->displayName->text());
		srcData->SetSequence(ui->sequence->value());
	}
	else
	{
		result = new AccountData;		
		result->Init(ui->displayName->text(), ui->address->text(), ui->secretKey->text(), ui->sequence->value());
	}
}

void AddAccDialog::on_secretKey_textChanged(const QString &arg1)
{
    QPalette palette;
    palette.setColor(QPalette::Text, Qt::black);
    QString tooltip;
	if (!arg1.isEmpty())
	{
        std::string addr = rippleGetAddressFromSecret(arg1.toStdString());
		if (addr.empty())
		{			
			palette.setColor(QPalette::Text, Qt::red);		
            tooltip = "invalid secret key";
			ui->address->setText("");
		}
		else
		{
            ui->address->setText(addr.c_str());
		}
	}
    ui->secretKey->setPalette(palette);
    ui->secretKey->setToolTip(tooltip);
    UpdateStatus();
}

void AddAccDialog::on_buttonBox_rejected()
{

}

void AddAccDialog::generateAddr()
{
    std::string addr,key;
    rippleGenerateAddress(addr, key);
    ui->address->setText(addr.c_str());
    ui->secretKey->setText(key.c_str());
    ui->sequence->setValue(1);    
    ui->displayName->setText(addr.substr(0,5).c_str());
    UpdateStatus();
}

void AddAccDialog::on_address_textChanged(const QString &arg1)
{
	std::string addr = arg1.toStdString();
	QPalette palette;
	palette.setColor(QPalette::Text, Qt::black);
	QString tooltip("");

	if (rippleValidateAddress(addr))
	{
        ui->infoQR->setEnabled(true);
        ui->accountQR->setEnabled(true);
		if (ui->displayName->text().isEmpty())
		{
			ui->displayName->setText(addr.substr(0, 5).c_str());
		}
	}
	else
	{
        ui->infoQR->setEnabled(false);
        ui->accountQR->setEnabled(false);
		if (!addr.empty())
		{
			palette.setColor(QPalette::Text, Qt::red);
			tooltip = "invalid account address";
		}
	}
	ui->address->setPalette(palette);
	ui->address->setToolTip(tooltip);

    UpdateStatus();
}

void AddAccDialog::UpdateStatus()
{
    std::string key = ui->secretKey->text().toStdString();
    std::string addr = ui->address->text().toStdString();
    std::string addrFromKey = rippleGetAddressFromSecret(key);

    switch (mode)
    {
        case edit:
        case newGenerate:
        {
			ui->buttonBox->button(QDialogButtonBox::Ok)->setEnabled(!ui->displayName->text().isEmpty());
            return;
        }
        case newRecipient:
        {
            if (!addr.empty() && !rippleValidateAddress(addr))
            {
                ui->status->setStyleSheet("color : red");
                ui->status->setText("Invalid account address");
                ui->buttonBox->button(QDialogButtonBox::Ok)->setEnabled(false);
            }
            else
            {
                ui->buttonBox->button(QDialogButtonBox::Ok)->setEnabled(!ui->displayName->text().isEmpty());
                ui->status->setStyleSheet("color : black");
                ui->buttonBox->button(QDialogButtonBox::Ok)->setEnabled(!ui->displayName->text().isEmpty());
                if (key.empty())
                    ui->status->setText("Enter public address of an existing account");
                else
                    ui->status->setText("Account address is valid");
            }
            return;
        }
        case newSender:
        {
            if ( (!key.empty()) && addrFromKey.empty())
            {
                ui->status->setStyleSheet("color : red");
                ui->status->setText("Invalid secret key");
                ui->buttonBox->button(QDialogButtonBox::Ok)->setEnabled(false);
            }
            else
            {
                ui->status->setStyleSheet("color : black");
                ui->buttonBox->button(QDialogButtonBox::Ok)->setEnabled(!ui->displayName->text().isEmpty());
                if (key.empty())
                    ui->status->setText("Enter secret key from existing account");
                else
                    ui->status->setText("Secret key is valid");
            }
            return;
        }
    }
}

void AddAccDialog::on_infoQR_clicked()
{
    QString addr = ui->address->text();
    int seq = ui->sequence->value();
    qrOnlyDialog dlg(this, qrOnlyDialog::qrAccountInfo, addr, &seq);
    if (dlg.exec()==QDialog::Accepted)
    {
        int val = dlg.GetInt();
        if (val!=0)
            ui->sequence->setValue(val);
    }
}

void AddAccDialog::on_seqHelp_clicked()
{
    QRTextContent dlg(sequenceHelp, "What is 'sequence'?",this);
    dlg.exec();
}

void AddAccDialog::on_displayName_textChanged(const QString &/*arg1*/)
{
    UpdateStatus();
}

void AddAccDialog::on_accountQR_clicked()
{
    QString addr = ui->address->text();
    QString desc("This QR code contains only the public ripple address. This content will not be sent to Ripple network using smartphone XRPoffline application. Use this only to transfer the ripple address to other device.");
    qrOnlyDialog dlg(this, qrOnlyDialog::qrOther, addr, &desc);
    dlg.exec();
}
