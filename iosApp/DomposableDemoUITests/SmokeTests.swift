import XCTest

final class SmokeTests: XCTestCase {
    func testSharedFeatureAndNotes() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", "Task workspace")).firstMatch.waitForExistence(timeout: 20))
        app.buttons["Try one feature on three platforms"].tap()
        let notes = app.textViews["task-notes"]
        XCTAssertTrue(notes.waitForExistence(timeout: 10))
        notes.tap()
        notes.typeText("Native feature notes")
        let portrait = XCTAttachment(screenshot: app.screenshot())
        portrait.name = "Shared task detail"
        portrait.lifetime = .keepAlways
        add(portrait)
        XCUIDevice.shared.orientation = .landscapeLeft
        XCTAssertTrue(notes.waitForExistence(timeout: 10))
        XCTAssertTrue((notes.value as? String)?.contains("Native feature notes") == true)
        let landscape = XCTAttachment(screenshot: app.screenshot())
        landscape.name = "Adaptive panes"
        landscape.lifetime = .keepAlways
        add(landscape)
    }
}
