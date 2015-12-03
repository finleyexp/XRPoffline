#include <QtWidgets>
#include "draglabel.h"
#include "dragwidget.h"
#include "data.h"
#include "mainwindow.h"

//////////////////////////////////////////////////////////////////////////
// DROP
//////////////////////////////////////////////////////////////////////////

DropWidget::DropWidget(QWidget *parent) : QFrame(parent)
{
	setFrameStyle(QFrame::Sunken | QFrame::StyledPanel);
	setAcceptDrops(true);
	droppedIndex = -1;
	senderOnly = false;
}

void DropWidget::dragEnterEvent(QDragEnterEvent *event)
{
	if (event->mimeData()->hasFormat("application/x-offlinedata") && event->source() != this)
	{
		if (senderOnly)
		{
			QByteArray itemData = event->mimeData()->data("application/x-offlinedata");
			QDataStream dataStream(&itemData, QIODevice::ReadOnly);
			int dataIndex;
			bool canSend;
			dataStream >> dataIndex >> canSend;
			if (!canSend)
			{
				event->ignore();
				return;
			}
		}
		event->acceptProposedAction();
	}
	else event->ignore();
}

void DropWidget::dropEvent(QDropEvent *event)
{
	if (event->mimeData()->hasFormat("application/x-offlinedata") && event->source() != this)
	{
		QByteArray itemData = event->mimeData()->data("application/x-offlinedata");
		QDataStream dataStream(&itemData, QIODevice::ReadOnly);

		int dataIndex;
		dataStream >> dataIndex;
		OnDrop(dataIndex, event);		
	}
	else event->ignore();
}

void DropWidget::createChildWidget()
{
    QList<QWidget *> widgets = findChildren<QWidget *>();
    foreach(QWidget * widget, widgets) delete widget;
    DragLabel *newIcon = new DragLabel(droppedIndex, this);
    newIcon->move((width() - newIcon->width()) >> 1, ((height() - newIcon->height()) >> 1) - 4);
    newIcon->show();
    newIcon->setAttribute(Qt::WA_DeleteOnClose);
}

void DropWidget::OnDrop(int dataIndex, QDropEvent *event)
{
    droppedIndex = int(dataIndex);

    createChildWidget();

	event->setDropAction(Qt::CopyAction);
	event->accept();
	
	emit itemChanged(this);
}

void DropWidget::on_itemDeleted(int) {}

void DropWidget::itemDeleted(int itemId)
{
	QList<DragLabel*> widgets = findChildren<DragLabel *>();
	foreach(DragLabel * widget, widgets)
	{
		if (widget->GetIndex() == itemId)
		{
			delete widget;
			droppedIndex = -1;
			emit itemChanged(this);
		}
	}
}

//////////////////////////////////////////////////////////////////////////
// DELETE
//////////////////////////////////////////////////////////////////////////

DeleteDropWidget::DeleteDropWidget(QWidget *parent) : DropWidget(parent) {}

void DeleteDropWidget::dragEnterEvent(QDragEnterEvent *event)
{
	if (event->mimeData()->hasFormat("application/x-offlinedata") && event->source() != this)
	{
		QByteArray itemData = event->mimeData()->data("application/x-offlinedata");
		QDataStream dataStream(&itemData, QIODevice::ReadOnly);
		int dataIndex;
		dataStream >> dataIndex;
		if (dataIndex)
		{
			event->setDropAction(Qt::MoveAction);
			event->accept();
			return;
		}
	}
	event->ignore();
}

void DeleteDropWidget::OnDrop(int dataIndex, QDropEvent* event)
{
	event->setDropAction(Qt::MoveAction);
	event->accept();
	emit itemDeleted(dataIndex);
}


//////////////////////////////////////////////////////////////////////////

//		WIDGET LIST

//////////////////////////////////////////////////////////////////////////

WidgetList::WidgetList(QWidget *parent) : QListWidget(parent), first(true)
{
    setDragEnabled(true);
    setViewMode(QListView::IconMode);
    setIconSize(QSize(10, 10));
    setSpacing(10);
    setAcceptDrops(true);
    setDropIndicatorShown(true);
}

void WidgetList::addPiece(QPixmap pixmap, int id, bool canSend)
{
	QListWidgetItem *pieceItem = new QListWidgetItem(this);
	pieceItem->setIcon(QIcon(pixmap));

	if (first)
	{
		first = false;
		QSize iconSize = pixmap.size();
        iconSize.setWidth(iconSize.width() + 7);
		static int yOffset = 21;
        iconSize.setHeight(iconSize.height() + yOffset);
		yOffset = 7;
		setIconSize(iconSize);
		setGridSize(iconSize);
	}

	pieceItem->setData(Qt::UserRole, QVariant(pixmap));
	pieceItem->setData(Qt::UserRole + 1, id);
	pieceItem->setData(Qt::UserRole + 2, canSend);
	pieceItem->setFlags(Qt::ItemIsEnabled | Qt::ItemIsSelectable | Qt::ItemIsDragEnabled);
}

void WidgetList::changePiece(QPixmap pixmap, QListWidgetItem *item)
{
    item->setIcon(QIcon(pixmap));
    item->setData(Qt::UserRole, QVariant(pixmap));
}

void WidgetList::deletePiece(int id)
{
	for (int i = 0; i < count(); i++) 
	{
		QListWidgetItem *item = this->item(i);
		int itemId = item->data(Qt::UserRole + 1).toInt();
		if (itemId == id)
		{
			delete takeItem(row(item));
			break;
		}
	}
}

void WidgetList::startDrag(Qt::DropActions /*supportedActions*/)
{
	QListWidgetItem *item = currentItem();

	QByteArray itemData;
	QDataStream dataStream(&itemData, QIODevice::WriteOnly);
	QPixmap pixmap = qvariant_cast<QPixmap>(item->data(Qt::UserRole));
	int id = item->data(Qt::UserRole + 1).toInt();
	bool canSend = item->data(Qt::UserRole + 2).toBool();
	
	dataStream << id << canSend << pixmap;

	QMimeData *mimeData = new QMimeData;
	mimeData->setData("application/x-offlinedata", itemData);

	QDrag *drag = new QDrag(this);
	drag->setMimeData(mimeData);
	drag->setHotSpot(QPoint(pixmap.width() / 2, pixmap.height() / 2));
	drag->setPixmap(pixmap);
	drag->exec(Qt::CopyAction | Qt::MoveAction, Qt::CopyAction);
}

//////////////////////////////////////////////////////////////////////////

//      FocusLineEdit

//////////////////////////////////////////////////////////////////////////

FocusLineEdit::FocusLineEdit(QWidget *parent) : QLineEdit(parent) {}
FocusLineEdit::~FocusLineEdit() {}

void FocusLineEdit::focusInEvent(QFocusEvent *e)
{
  QLineEdit::focusInEvent(e);
  emit(focusChanged(true));
}

void FocusLineEdit::focusOutEvent(QFocusEvent *e)
{
  QLineEdit::focusOutEvent(e);
  emit(focusChanged(false));
}
