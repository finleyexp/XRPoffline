#ifndef QRONLYDIALOG_H
#define QRONLYDIALOG_H

#include <QDialog>

namespace Ui {
class qrOnlyDialog;
}

class QDoubleSpinBox;
class QSpinBox;

class qrOnlyDialog : public QDialog
{
    Q_OBJECT

public:
    enum qrMode{
        qrServerState,
        qrAccountInfo,
        qrOther
    };

    explicit qrOnlyDialog(QWidget *parent, qrMode mode, const QString& address, void* data);
    ~qrOnlyDialog();

    double GetDouble();
    int GetInt();


private slots:
    void on_buttonBox_accepted();

    void on_buttonBox_rejected();

private:
    Ui::qrOnlyDialog *ui;
    qrMode mode;
    QDoubleSpinBox* doubleWidget;
    QSpinBox* intWidget;
};

#endif // QRONLYDIALOG_H
