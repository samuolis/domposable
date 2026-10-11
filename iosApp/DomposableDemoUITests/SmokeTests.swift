import XCTest

final class SmokeTests: XCTestCase {
    private func ready(_ app: XCUIApplication, _ title: String) {
        XCTAssertTrue(app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", title)).firstMatch.waitForExistence(timeout: 30))
    }
    private func capture(_ app: XCUIApplication, _ name: String) {
        // App screenshots can crop with portrait coordinates during iPad rotation.
        // Capture the display after two consecutive rendered frames agree.
        var previous: Data?
        var settled: XCUIScreenshot?
        let stable = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            let current = XCUIScreen.main.screenshot()
            let data = current.pngRepresentation
            defer { previous = data }
            guard previous == data else { return false }
            settled = current
            return true
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [stable], timeout: 15), .completed)
        let attachment = XCTAttachment(screenshot: settled ?? XCUIScreen.main.screenshot())
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
        let landscape = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            app.frame.width > app.frame.height
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [landscape], timeout: 10), .completed)
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
    func testShopping() {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        app.buttons["Shop demo"].tap()
        ready(app, "FIELDWORK")
        XCTAssertTrue(app.descendants(matching: .any).matching(identifier: "shop-hero.png-loaded").firstMatch.waitForExistence(timeout: 30))
        capture(app, "shop")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Shop all")).firstMatch.tap()
        ready(app, "Shop the collection")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Add Canvas weekender to bag")).firstMatch.tap()
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Bag (1)")).firstMatch.tap()
        ready(app, "Your bag")
        ready(app, "Canvas weekender")
    }
}
