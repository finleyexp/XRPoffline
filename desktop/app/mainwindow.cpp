#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QFileDialog>
#include <QFile>
#include <QMessageBox>
#include <QTextStream>
#include <QListWidget>
#include "draglabel.h"
#include "data.h"
#include "qrdialog.h"
#include "addaccdialog.h"
#include "QVBoxLayout"
#include "qronlydialog.h"
#include "qrtextcontent.h"
#include <QMenu>
#include <QTimer>
#include <QInputDialog>

QImage* GenerateImageFromData(AccountData* data, QFontMetrics &metric);
QImage* GenerateImageFromDataDonate(AccountData* data, QFontMetrics &metric);

QString sequenceHelp(
	"Each ripple account has associated transaction counter called sequence. Every signed transaction contains sequence number which has to match the account sequence. First transaction ever executed using a new account has sequence 1, the next one has to be increased by exactly one.\n\n"
        "XRPoffline can’t get the account sequence from the network, so it has to be entered manually, with help of QR code containing account_info command. Once the transaction is submitted to the network, it is necessary to increase sequence for next transaction.\n\n"
        "Note: One way to invalidate generated signed transaction (which was not yet submitted) is to generate another one with the same sequence number. Submit the new one and the old will never succeed as its sequence was already used."
        );

MainWindow::MainWindow(QWidget *parent) :
QMainWindow(parent), ui(new Ui::MainWindow), settings("XRPoffline", "XRPoffline"), wallet(*Wallet::GetWallet())
{	
    ui->setupUi(this);
	
	ui->sequence->hide();	
    ui->seqHelp->hide();
	ui->frameFrom->setFrameStyle(QFrame::Sunken | QFrame::StyledPanel);
	ui->frameFrom->SenderOnly();
	ui->frameTo->setFrameStyle(QFrame::Sunken | QFrame::StyledPanel);	

	ui->accountList->setSpacing(10);
	ui->accountList->setDragEnabled(true);
	ui->accountList->setViewMode(QListView::IconMode);

    QStyle *style = QApplication::style();

    ui->seqHelp->setIcon(style->standardIcon(QStyle::SP_MessageBoxQuestion, 0, this));


    QMenu* menu   = new QMenu(ui->buttonAdd);
    QAction* act0 = new QAction("Generate &new account...",ui->buttonAdd);
    QAction* act1 = new QAction("Add &Sender/Recipient account...",ui->buttonAdd);
    QAction* act2 = new QAction("Add &Recipient account...",ui->buttonAdd);
    act0->setObjectName("act0");
    act1->setObjectName("act1");
    act2->setObjectName("act2");
    menu->addAction(act0);
    menu->addAction(act1);
    menu->addAction(act2);
    ui->buttonAdd->setMenu(menu);
    connect(act0,SIGNAL(triggered()),this,SLOT(generateNewAccount()));
    connect(act1,SIGNAL(triggered()),this,SLOT(addSenderAccount()));
    connect(act2,SIGNAL(triggered()),this,SLOT(addRecipientAccount()));

//donate
    ui->donate->setSpacing(10);
    ui->donate->setDragEnabled(true);
    ui->donate->setViewMode(QListView::IconMode);
    AccountData donateAccount;
    donateAccount.Init("~rippleOffline", "rUsW2dU5NnAhg6f78soejmFFpfRvzefYfs", "", 0);
    int donateId = wallet.AddAccount(donateAccount);
    WidgetList* list = ui->donate;
    QFontMetrics metrics(list->font());

    AccountData* acc = wallet.GetAccount(donateId);
    QImage* img = GenerateImageFromDataDonate(acc, metrics );
    QPixmap pixmap = QPixmap::fromImage(*img);
    delete img;
    list->addPiece(pixmap, donateId, acc->CanSend());

    list->setStyleSheet("background-color: transparent;");

//load wallet
	QTimer::singleShot(100, this, SLOT(InitWallet()));
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::on_action_Open_triggered()
{
}

void MainWindow::on_action_Save_triggered()
{
}

void MainWindow::itemAdded(int itemId)
{
    WidgetList* list = ui->accountList;
    QFontMetrics metrics(list->font());
	
	AccountData* acc = wallet.GetAccount(itemId);

    QImage* img = GenerateImageFromData(acc, metrics );
	QPixmap pixmap = QPixmap::fromImage(*img);
	delete img;
    list->addPiece(pixmap, itemId, acc->CanSend());
}


void MainWindow::on_itemChanged(DropWidget* widget)
{
	if (widget == ui->frameFrom)
	{
		AccountData* d = wallet.GetAccount(ui->frameFrom->GetDroppedIndex());
		if (d)
		{
			ui->sequence->setValue(d->GetSequence());
			ui->sequence->show();
            ui->seqHelp->show();
		}
        else
        {
            ui->sequence->hide();
            ui->seqHelp->hide();
        }
	}
}

void MainWindow::on_buttonQR_clicked()
{
	AccountData* dataFrom = wallet.GetAccount(ui->frameFrom->GetDroppedIndex());
	AccountData* dataTo = wallet.GetAccount(ui->frameTo->GetDroppedIndex());	
    if (dataFrom && dataTo)
    {		
        if (dataFrom->GetAddr()==dataTo->GetAddr())
        {
            QRTextContent dlg("The ripple protocol does not support sending XRP within the same account.", "Error",this);
            dlg.exec();
        }
        else
        {
			int sequence = dataFrom->GetSequence();

            QRDialog dlg(this, dataFrom, dataTo, ui->value->value());
            dlg.exec();

			if (dataFrom->GetSequence() != sequence)
				SaveWallet();

			AccountData* data = wallet.GetAccount(ui->frameFrom->GetDroppedIndex());
			if (data)
				ui->sequence->setValue(data->GetSequence());
        }
    }
}

void MainWindow::on_buttonAdd_clicked()
{
}

void MainWindow::on_itemDeleted(int itemId)
{
	AccountData* acc = wallet.GetAccount(itemId);
	if (!acc) return;

	QMessageBox msgBox;
	msgBox.setText(tr("Do you really want to delete account ") + acc->GetName() + "?");
	msgBox.setStandardButtons(QMessageBox::Yes | QMessageBox::No);
	msgBox.setDefaultButton(QMessageBox::No);
	msgBox.setWindowTitle(tr("Delete Account"));
	if (msgBox.exec() != QMessageBox::Yes) return;

	ui->frameFrom->itemDeleted(itemId);
	ui->frameTo->itemDeleted(itemId);
	ui->accountList->deletePiece(itemId);
	wallet.DeleteAccount(itemId);
	SaveWallet();
}

void MainWindow::on_accountList_doubleClicked(const QModelIndex &index)
{
	QListWidgetItem *item = ui->accountList->item(index.row());
	int dataId = item->data(Qt::UserRole + 1).toInt();    
	AccountData* srcData = wallet.GetAccount(dataId);
    if (srcData)
    {
        QString originalName = srcData->GetName();
		int originalSeq = srcData->GetSequence();
        AddAccDialog dlg(srcData, AddAccDialog::edit, this);
        dlg.exec();
		bool save = false;
		if (srcData->GetSequence() != originalSeq)
		{
			UpdateSequence();
			save = true;
		}
        if (srcData->GetName()!=originalName) //name changed
        {
			save = true;
            QFontMetrics metrics(ui->accountList->font());
            QImage* img = GenerateImageFromData(srcData, metrics );
            QPixmap pixmap = QPixmap::fromImage(*img);
            delete img;
            ui->accountList->changePiece(pixmap, item);

            if (ui->frameFrom->GetDroppedIndex()==dataId)
                ui->frameFrom->createChildWidget();

            if (ui->frameTo->GetDroppedIndex()==dataId)
                ui->frameTo->createChildWidget();
        }
		if (save)
			SaveWallet();
    }
}

void MainWindow::UpdateSequence()
{
	AccountData* data = wallet.GetAccount(ui->frameFrom->GetDroppedIndex());
    if (data)
            ui->sequence->setValue(data->GetSequence());
}

void MainWindow::on_sequence_valueChanged(int arg1)
{
	AccountData* data = wallet.GetAccount(ui->frameFrom->GetDroppedIndex());
    if (data)
        data->SetSequence(arg1);
	SaveWallet();
}

void MainWindow::on_seqHelp_clicked()
{
    QRTextContent dlg(sequenceHelp, "What is 'sequence'?",this);
    dlg.exec();
}

void MainWindow::generateNewAccount()
{
    AddAccDialog dlg(NULL, AddAccDialog::newGenerate, this);
	dlg.exec();
	if (dlg.GetResult())
	{
		int accId = wallet.AddAccount(*dlg.GetResult());
		itemAdded(accId);
		SaveWallet();
	}
}

void MainWindow::addSenderAccount()
{
    AddAccDialog dlg(NULL, AddAccDialog::newSender, this);
    dlg.exec();
	if (dlg.GetResult())
	{
		int accId = wallet.AddAccount(*dlg.GetResult());
		itemAdded(accId);
		SaveWallet();
	}
}

void MainWindow::addRecipientAccount()
{
    AddAccDialog dlg(NULL, AddAccDialog::newRecipient, this);
    dlg.exec();
	if (dlg.GetResult())
	{
		int accId = wallet.AddAccount(*dlg.GetResult());
		itemAdded(accId);
		SaveWallet();
	}
}


void MainWindow::InitWallet()
{
	QString fileLocation = GetWalletFileName();
	QFile walletFile(fileLocation);

	Qt::WindowFlags flags = Qt::WindowTitleHint | Qt::WindowCloseButtonHint;

	if (walletFile.exists())
	{
		QString fileContent;
		QFile walletFile(fileLocation);

		bool ok = false;
		while (!ok)
		{
			ok = walletFile.open(QIODevice::ReadOnly | QIODevice::Text);
			if (!ok)
			{
				QMessageBox msgBox;
				msgBox.setText("Failed not open wallet from file:\n" + fileLocation);
				msgBox.setStandardButtons(QMessageBox::Retry | QMessageBox::Cancel);
				msgBox.setDefaultButton(QMessageBox::Retry);
				msgBox.setWindowTitle(tr("Unlock wallet"));
				int ret = msgBox.exec();
				if (ret == QMessageBox::Cancel)
					break;
			}
		}
		if (!ok) { qApp->quit(); return; }
		QTextStream strm(&walletFile);
		fileContent.append(strm.readAll());
		walletFile.close();

		//enter password for existing wallet & open it
		ok = false;
		while (!ok)
		{
			QString password = QInputDialog::getText(this, tr("Unlock wallet"), tr("Enter wallet password:"), QLineEdit::Password, "", &ok, flags);
			if (ok && password.isEmpty()) { ok = false; continue; }
			if (ok)
			{
				std::vector<int> accountIds;
				try {
					accountIds = wallet.LoadFromJson(fileContent, password);
				}
				catch (std::exception& e)
				{
					QMessageBox msgBox;
					msgBox.setText(e.what());
					msgBox.setStandardButtons(QMessageBox::Retry | QMessageBox::Cancel);
					msgBox.setDefaultButton(QMessageBox::Retry);
					msgBox.setWindowTitle(tr("Unlock wallet"));
					if (msgBox.exec() == QMessageBox::Cancel) { qApp->quit(); return; }
					ok = false;
					continue;
				}
				for (size_t i = 0; i < accountIds.size(); i++)
				{
					itemAdded(accountIds[i]);
				}
			}
			if (!ok) { qApp->quit();	return; }
		}
	}
	else
	{
		if (!SetNewPassword(tr("Initialize wallet")))
		{
			qApp->quit();
		}
		SaveWallet();
	}
}

bool MainWindow::SetNewPassword(const QString& title)
{
	Qt::WindowFlags flags = Qt::WindowTitleHint | Qt::WindowCloseButtonHint;

	//create new password & confirm it
	bool ok = false;
	QString password;
	while (!ok)
	{
		password = QInputDialog::getText(this, title, tr("Enter new wallet password:"), QLineEdit::Normal, "", &ok, flags);
		if (!ok) return false;
        if (password.isEmpty())
        {
            ok = false;
            continue;
        }

		QString password2 = QInputDialog::getText(this, title, tr("Confirm new wallet password:"), QLineEdit::Normal, "", &ok, flags);
		if (!ok) return false;

		ok = password == password2;
		if (!ok)
		{
			QMessageBox msgBox;
            msgBox.setText(tr("Entered passwords do not match."));
			msgBox.setStandardButtons(QMessageBox::Retry | QMessageBox::Cancel);
			msgBox.setDefaultButton(QMessageBox::Retry);
			msgBox.setWindowTitle(title);
			if (msgBox.exec() == QMessageBox::Cancel) return false;
            continue;
		}
	}
	
	wallet.SetNewPassword(password);

	QString fileLocation = GetWalletFileName();

	QMessageBox msgBox;
	msgBox.setText("Wallet password set to: " + password + "\nThis password is used to encrypt stored secret keys.\nWallet file will be auto saved after each operation at:\n" + fileLocation);
	msgBox.setWindowTitle(tr("Initialize wallet"));
	msgBox.setStandardButtons(QMessageBox::Ok);
	msgBox.exec();
		
	return true;
}

bool MainWindow::SaveWallet()
{
	QString walletData;
	try {
		walletData = wallet.SaveAccounts();
	}
	catch (...)
	{
		QMessageBox msgBox;
		msgBox.setText(tr("Failed to serialize wallet."));
		msgBox.setStandardButtons(QMessageBox::Ok);
		msgBox.setWindowTitle(tr("Saving wallet"));
		msgBox.exec();
		return false;
	}

	if (walletData.isEmpty())
	{
		return false;
	}
	
	QString fileLocation = GetWalletFileName();
	QFile walletFile(fileLocation);

	bool ok = false;
	while (!ok)
	{
		ok = walletFile.open(QIODevice::WriteOnly);
		if (!ok) 
		{
			QMessageBox msgBox;
			msgBox.setText("Failed not save wallet to file:\n" + fileLocation);
			msgBox.setStandardButtons(QMessageBox::Retry | QMessageBox::Cancel);
			msgBox.setDefaultButton(QMessageBox::Retry);
			int ret = msgBox.exec();
			if (ret == QMessageBox::Cancel)
				break;
		}
	}
	if (!ok)
		return false;
		
	QTextStream outstream(&walletFile);
	outstream << walletData;
	walletFile.close();
	
	return true;
}


#ifdef WIN32
#include <windows.h>
#include <Shlobj.h>
static std::string GetSpecialFolderPath(int nFolder, bool fCreate = true)
{
	char pszPath[MAX_PATH] = "";

	if (SHGetSpecialFolderPathA(NULL, pszPath, nFolder, fCreate))
	{
		return pszPath;
	}
	return "";
}
#endif


QString MainWindow::GetWalletFileName()
{
	std::string result;
	// Windows < Vista: C:\Documents and Settings\Username\Application Data\XRPoffline
	// Windows >= Vista: C:\Users\Username\AppData\Roaming\XRPoffline
	// Unix: ~/.XRPoffline
#ifdef WIN32
	result = GetSpecialFolderPath(CSIDL_APPDATA) + "\\XRPoffline";
#else
	std::string pathRet;
	char* pszHome = getenv("HOME");
	if (pszHome == NULL || strlen(pszHome) == 0)
		pathRet = "/";
	else
		pathRet = pszHome;
	result = pathRet + "/.XRPoffline";
#endif


#ifdef WIN32
	if (!result.empty())
	{
		CreateDirectoryA(result.c_str(), NULL);
	}
	result += "\\ripple.dat";
#else
	result += "/ripple.dat";
#endif

	return QString(result.c_str());
}
