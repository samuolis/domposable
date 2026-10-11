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
}
