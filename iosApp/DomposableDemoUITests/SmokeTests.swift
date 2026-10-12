import XCTest

final class SmokeTests: XCTestCase {
    private func ready(_ app: XCUIApplication, _ title: String) {
        XCTAssertTrue(app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", title)).firstMatch.waitForExistence(timeout: 30))
    }
    private func capture(_ app: XCUIApplication, _ name: String) {
        // App screenshots can crop with portrait coordinates during iPad rotation.
        // Require a full second of identical rendered frames before capture.
        var previous: Data?
        var settled: XCUIScreenshot?
        var unchangedSince = Date()
        let stable = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            let current = XCUIScreen.main.screenshot()
            let data = current.pngRepresentation
            defer { previous = data }
            guard previous == data else { unchangedSince = Date(); return false }
            guard Date().timeIntervalSince(unchangedSince) >= 1 else { return false }
            settled = current
            return true
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [stable], timeout: 15), .completed)
        let attachment = XCTAttachment(screenshot: settled ?? XCUIScreen.main.screenshot())
        attachment.name = "golden-\(name)"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
    private func artworkIsRendered(_ element: XCUIElement) -> Bool {
        // A decoded-image callback can precede its first GPU frame on a slow simulator.
        // The bundled hero has light and dark texture; an unpainted image is a flat surface.
        guard let image = element.screenshot().image.cgImage,
              image.bitsPerComponent == 8, image.bitsPerPixel >= 24,
              let data = image.dataProvider?.data,
              let bytes = CFDataGetBytePtr(data) else { return false }
        let stride = image.bitsPerPixel / 8
        var darkest = 255
        var lightest = 0
        for y in Swift.stride(from: 0, to: image.height, by: max(1, image.height / 20)) {
            for x in Swift.stride(from: 0, to: image.width, by: max(1, image.width / 20)) {
                let offset = y * image.bytesPerRow + x * stride
                let light = (Int(bytes[offset]) + Int(bytes[offset + 1]) + Int(bytes[offset + 2])) / 3
                darkest = min(darkest, light)
                lightest = max(lightest, light)
            }
        }
        return lightest - darkest > 40
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
        app.buttons["More components"].tap()
        ready(app, "More shared components")
        capture(app, "extended-components")
        app.buttons["Save preferences"].tap()
        ready(app, "Preferences saved")
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
        let hero = app.descendants(matching: .any).matching(identifier: "shop-hero.png-loaded").firstMatch
        XCTAssertTrue(hero.waitForExistence(timeout: 30))
        let painted = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            self.artworkIsRendered(hero)
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [painted], timeout: 30), .completed)
        capture(app, "shop")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Shop all")).firstMatch.tap()
        ready(app, "Shop the collection")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Add Canvas weekender to bag")).firstMatch.tap()
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Bag (1)")).firstMatch.tap()
        ready(app, "Your bag")
        ready(app, "Canvas weekender")
    }
    private func tapVisible(_ app: XCUIApplication, _ label: String) {
        let button = app.buttons[label]
        for _ in 0..<6 {
            if button.exists && button.isHittable { button.tap(); return }
            app.swipeUp()
        }
        XCTFail("Could not reach button: \(label)")
    }
    func testCollectionLayoutsAndTableInput() {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Components")).firstMatch.tap()
        ready(app, "Component catalog")
        tapVisible(app, "More components")
        ready(app, "More shared components")
        tapVisible(app, "Large collections lab")
        ready(app, "Large collections")
        tapVisible(app, "More collection recipes")
        ready(app, "Collection recipes")
        capture(app, "collection-measured")
        for (layout, name) in [("Horizontal row", "collection-row"), ("Responsive grid", "collection-grid"), ("Semantic table", "collection-table")] {
            // Return to the selector before changing layout; no keyboard is open during goldens.
            let selector = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Collection layout,")).firstMatch
            for _ in 0..<6 {
                if selector.exists && selector.isHittable { break }
                app.swipeDown()
            }
            XCTAssertTrue(selector.isHittable)
            selector.tap()
            let option = app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", layout)).firstMatch
            XCTAssertTrue(option.waitForExistence(timeout: 10))
            option.tap()
            ready(app, "Collection layout, Collection layout: \(layout)")
            capture(app, name)
        }
        ready(app, "Inventory drafts")
        let draft = app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", "Item 1 draft")).firstMatch
        XCTAssertTrue(draft.waitForExistence(timeout: 10))
        draft.tap()
        draft.typeText("iOS table draft")
        XCTAssertTrue((draft.value as? String)?.contains("iOS table draft") == true)
    }

}
