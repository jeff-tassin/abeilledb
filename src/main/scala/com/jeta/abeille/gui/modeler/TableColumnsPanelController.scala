package com.jeta.abeille.gui.modeler

import com.jeta.abeille.database.model.TableId
import com.jeta.abeille.gui.store.ColumnInfo
import com.jeta.foundation.gui.components.TSController
import com.jeta.open.gui.framework.UIDirector

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.EventObject


class TableColumnsPanelController(private var view: ColumnsPanel) extends TSController(view) with UIDirector {

  assignAction(ColumnsPanel.ID_GENERATE_INSERT_SQL, new GenerateInsertSQLAction)

  override def updateComponents(eventObject: EventObject): Unit = {
  }

  /**
   * Generates an INSERT command with selected columns
   */
  class GenerateInsertSQLAction extends ActionListener {
    override def actionPerformed(evt: ActionEvent): Unit = {
      val model = view.getModel
      val tableId = model.getTableId
      val cols = view.getSelectedItems

      cols.nonEmpty match {
        case true => {
          val colnames = cols.map( _.getColumnName ).mkString(", ")
          val sql = s"INSERT INTO $tableId ($colnames) values($colnames)"
          val clip = Toolkit.getDefaultToolkit.getSystemClipboard
          clip.setContents(new StringSelection(sql), null)
        }
        case false =>
      }
    }
  }
}
