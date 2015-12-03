#ifndef QRWIDGET_HPP
#define QRWIDGET_HPP

#include <QWidget>

class QRWidget : public QWidget{
    Q_OBJECT
private:
    QString data;
public:
    explicit QRWidget(QWidget *parent = 0);
    void setQRData(const QString& data);
    virtual int heightForWidth( int w ) const Q_DECL_OVERRIDE { return w; }
    void mousePressEvent ( QMouseEvent * event ) Q_DECL_OVERRIDE;
    void HideQR() {hidden = true;}
    void ShowQR() {hidden = false;}
protected:
    void paintEvent(QPaintEvent *);
    bool hidden;
};

#endif // QRWIDGET_HPP
