#ifndef DRAGWIDGET_H
#define DRAGWIDGET_H

#include <QFrame>
#include <QListWidget>
#include <QWidget>
#include <QLineEdit>

QT_BEGIN_NAMESPACE
class QDragEnterEvent;
class QDropEvent;
QT_END_NAMESPACE


class WidgetList : public QListWidget
{
	Q_OBJECT
public:
    explicit WidgetList(QWidget *parent = 0);        
	void addPiece(QPixmap pixmap, int id, bool canSend);
    void changePiece(QPixmap pixmap, QListWidgetItem *item);
	void deletePiece(int id);
protected:
	void startDrag(Qt::DropActions supportedActions) Q_DECL_OVERRIDE;
    bool first;
};

class DropWidget : public QFrame
{
	Q_OBJECT
public:
	DropWidget(QWidget *parent = 0);
	int GetDroppedIndex() { return droppedIndex; }
	void SenderOnly() { senderOnly = true; }
	virtual void OnDrop(int dataIndex, QDropEvent *event);
    void createChildWidget();
	void itemDeleted(int itemId);
protected:
	void dragEnterEvent(QDragEnterEvent *event) Q_DECL_OVERRIDE;
	void dropEvent(QDropEvent *event) Q_DECL_OVERRIDE;
	int droppedIndex;
	bool senderOnly;
signals:
	void itemChanged(DropWidget*);

private slots:
	void on_itemDeleted(int itemId);
};

class DeleteDropWidget : public DropWidget
{
	Q_OBJECT
public:
	DeleteDropWidget(QWidget *parent = 0);
	void dragEnterEvent(QDragEnterEvent *event) Q_DECL_OVERRIDE;
    virtual void OnDrop(int dataIndex, QDropEvent *event) Q_DECL_OVERRIDE;
signals:
	void itemDeleted(int itemId);
};

//qlineedit which sends signal when receives/loses focus
class FocusLineEdit : public QLineEdit
{
  Q_OBJECT

public:
  FocusLineEdit(QWidget *parent = 0);
  ~FocusLineEdit();

signals:
  void focusChanged(bool receivedFocus);

protected:
  virtual void focusInEvent(QFocusEvent *e);
  virtual void focusOutEvent(QFocusEvent *e);
};

#endif // DRAGWIDGET_H
