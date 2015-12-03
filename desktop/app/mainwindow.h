#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QSettings>
#include "data.h"

namespace Ui {
class MainWindow;
}

class DropWidget;

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

	void itemAdded(int itemId);

    void UpdateSequence();

private slots:
    void on_action_Open_triggered();
    void on_action_Save_triggered();
    void on_buttonQR_clicked();
    void on_buttonAdd_clicked();
	void on_itemChanged(DropWidget* dropArea);
	void on_itemDeleted(int itemId);

    void on_accountList_doubleClicked(const QModelIndex &index);

    void on_sequence_valueChanged(int arg1);

    void on_seqHelp_clicked();

    void generateNewAccount();
    void addSenderAccount();
    void addRecipientAccount();

	void InitWallet();

private:
	QString GetWalletFileName();
	bool SaveWallet();

	bool SetNewPassword(const QString& title);

private:
	Wallet& wallet;

    Ui::MainWindow *ui;    
	QSettings settings;
};

#endif // MAINWINDOW_H
