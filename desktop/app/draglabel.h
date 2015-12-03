#ifndef DRAGLABEL_H
#define DRAGLABEL_H

#include <QLabel>
#include <QImage>

QT_BEGIN_NAMESPACE
class QDragEnterEvent;
class QDragMoveEvent;
class QFrame;
QT_END_NAMESPACE

class DragLabel : public QLabel
{
public:
    DragLabel(int dataIndex, QWidget *parent);
	int GetIndex() { return dataIndex; }

private:
    int dataIndex;
};


#endif // DRAGLABEL_H
