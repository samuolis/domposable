import XCTest

final class SmokeTests: XCTestCase {
    func testSharedFeatureAndNotes() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["Task workspace"].waitForExistence(timeout: 20))
        app.buttons["Try one feature on three platforms"].tap()
        let notes = app.textViews.firstMatch
        XCTAssertTrue(notes.waitForExistence(timeout: 10))
        notes.tap()
        notes.typeText("Native feature notes")
        XCUIDevice.shared.orientation = .landscapeLeft
        XCTAssertTrue(notes.waitForExistence(timeout: 10))
        XCTAssertTrue((notes.value as? String)?.contains("Native feature notes") == true)
    }
}
