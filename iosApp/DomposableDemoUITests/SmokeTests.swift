import XCTest

final class SmokeTests: XCTestCase {
    private func ready(_ app: XCUIApplication, _ title: String) {
        XCTAssertTrue(app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", title)).firstMatch.waitForExistence(timeout: 30))
    }
    private func capture(_ app: XCUIApplication, _ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = "golden-\(name)"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
    func testSharedFeatureAndNotes() {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        capture(app, "tasks")
        app.buttons["Try one feature on three platforms"].tap()
        let notes = app.textViews["task-notes"]
        XCTAssertTrue(notes.waitForExistence(timeout: 10))
        notes.tap()
        notes.typeText("Native feature notes")
        XCUIDevice.shared.orientation = .landscapeLeft
        XCTAssertTrue(notes.waitForExistence(timeout: 10))
        XCTAssertTrue((notes.value as? String)?.contains("Native feature notes") == true)
        // Hide the keyboard and leave a stable adaptive scene for the baseline.
        app.buttons["Back to tasks"].tap()
        ready(app, "Task workspace")
        capture(app, "adaptive")
    }
    func testCatalogAndRecipes() {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Components")).firstMatch.tap()
        ready(app, "Component catalog")
        capture(app, "catalog")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Recipes")).firstMatch.tap()
        ready(app, "Navigation recipes")
        capture(app, "recipes")
        app.buttons["Open message"].tap()
        XCTAssertTrue(app.textViews["recipe-draft"].waitForExistence(timeout: 10))
    }
}
