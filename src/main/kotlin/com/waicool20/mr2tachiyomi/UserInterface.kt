/*
 * GPLv3 License
 *
 *  Copyright (c) mr2tachiyomi by waicool20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.waicool20.mr2tachiyomi

import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import tornadofx.*
import java.nio.file.Paths

class UserInterface : App(MainView::class)

class MainView : View() {
    override val root: HBox by fxml("/main.fxml")
    private val pathTextField: TextField by fxid()
    private val openButton: Button by fxid()
    private val runButton: Button by fxid()

    init {
        title = "MR2Tachiyomi"
        primaryStage.isResizable = false
    }

    override fun onDock() {
        super.onDock()

        openButton.setOnAction {
            chooseFile(
                title = "Select mangarock.db",
                filters = arrayOf(
                    FileChooser.ExtensionFilter(
                        "SQLite Database (*.db); Android Backup (*.ab)",
                        "*.db", "*.ab"
                    )
                ),
                owner = currentWindow
            ).firstOrNull()?.let {
                pathTextField.text = it.absolutePath
            }
        }

        runButton.setOnAction {
            if (pathTextField.text.isEmpty()) {
                error("Please choose your MangaRock DB or Android Backup first")
                return@setOnAction
            }
            chooseFile(
                title = "Select output file",
                filters = arrayOf(
                    FileChooser.ExtensionFilter("Json File (*.json)", "*.json"),
                    FileChooser.ExtensionFilter("Csv File (*.csv)", "*.csv")
                ),
                owner = currentWindow,
                mode = FileChooserMode.Save
            ).firstOrNull()?.let {
                when (val results = MR2Tachiyomi.convert(Paths.get(pathTextField.text), it.toPath())) {
                    is MR2Tachiyomi.Result.ConversionComplete -> {
                        information(
                            "Conversion complete",
                            """
                                |Succesfully processed ${results.success.size} manga
                                |Failed to process ${results.failed.size} manga
                                |See logs for more information""".trimMargin()
                        )
                    }
                    is MR2Tachiyomi.Result.FailedWithException -> error(
                        "Conversion failed",
                        """
                            |Error: ${results.exception.message}
                            |See logs for more information""".trimMargin()
                    )
                }
            }
        }
    }
}