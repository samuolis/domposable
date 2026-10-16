import XCTest
import Vision
import ImageIO

final class SmokeTests: XCTestCase {
    func testAppComponentsAndModifiers() {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Components")).firstMatch.tap()
        ready(app, "Component catalog")
        tapVisible(app, "More components")
        ready(app, "More shared components")
        tapVisible(app, "Custom components and modifiers")
        ready(app, "App-owned components and modifiers")
        capture(app, "extensions", renderedText: "Custom counter: 0")
        tapVisible(app, "Increment custom counter")
        ready(app, "Custom counter: 1")
        tapVisible(app, "Change frame color")
        ready(app, "Custom counter: 1")
    }
    func testSharedComposition() {
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Recipes")).firstMatch.tap()
        ready(app, "Navigation recipes")
        tapVisible(app, "Shared cards and adaptive panes")
        ready(app, "Design workshop")
        capture(app, "composition", renderedText: "Design workshop")
    }
    private func ready(_ app: XCUIApplication, _ title: String) {
        XCTAssertTrue(app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", title)).firstMatch.waitForExistence(timeout: 30))
    }
    private func renderedText(_ screenshot: XCUIScreenshot, contains text: String) -> Bool {
        guard let image = screenshot.image.cgImage else { return false }
        let orientation: CGImagePropertyOrientation
        switch screenshot.image.imageOrientation {
        case .up: orientation = .up
        case .down: orientation = .down
        case .left: orientation = .left
        case .right: orientation = .right
        case .upMirrored: orientation = .upMirrored
        case .downMirrored: orientation = .downMirrored
        case .leftMirrored: orientation = .leftMirrored
        case .rightMirrored: orientation = .rightMirrored
        @unknown default: return false
        }
        let request = VNRecognizeTextRequest()
        request.recognitionLevel = .accurate
        request.recognitionLanguages = ["en-US"]
        request.usesLanguageCorrection = false
        do {
            try VNImageRequestHandler(cgImage: image, orientation: orientation).perform([request])
        } catch { return false }
        let recognized = (request.results ?? []).compactMap { $0.topCandidates(1).first?.string }.joined(separator: " ")
        func normalize(_ value: String) -> String {
            value.split(whereSeparator: { $0.isWhitespace }).joined(separator: " ").lowercased()
        }
        return normalize(recognized).contains(normalize(text))
    }
    private func capture(_ app: XCUIApplication, _ name: String, renderedText text: String) {
        // App screenshots can crop with portrait coordinates during iPad rotation.
        // Accessibility can expose the next scene while the GPU still paints the previous one.
        // Require both the expected visible text and a full second of identical frames.
        var previous: Data?
        var settled: XCUIScreenshot?
        var unchangedSince = Date()
        let stable = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            let current = XCUIScreen.main.screenshot()
            let data = current.pngRepresentation
            defer { previous = data }
            guard previous == data else { unchangedSince = Date(); return false }
            guard Date().timeIntervalSince(unchangedSince) >= 1 else { return false }
            guard self.renderedText(current, contains: text) else { return false }
            settled = current
            return true
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [stable], timeout: 15), .completed,
                       "Capture \(name) did not paint stable text: \(text)")
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
        capture(app, "tasks", renderedText: "Task workspace")
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
        capture(app, "adaptive", renderedText: "Task workspace")
    }
    func testCatalogAndRecipes() {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Components")).firstMatch.tap()
        ready(app, "Component catalog")
        capture(app, "catalog", renderedText: "Component catalog")
        XCTAssertFalse(renderedText(XCUIScreen.main.screenshot(), contains: "More shared components"),
                       "The rendered-scene guard must reject the previous catalog")
        app.buttons["More components"].tap()
        ready(app, "More shared components")
        capture(app, "extended-components", renderedText: "More shared components")
        app.buttons["Save preferences"].tap()
        ready(app, "Preferences saved")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Recipes")).firstMatch.tap()
        ready(app, "Navigation recipes")
        capture(app, "recipes", renderedText: "Navigation recipes")
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
        capture(app, "shop", renderedText: "FIELDWORK")
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
    private func tapMaterialControl(_ app: XCUIApplication, _ label: String, identifier: String? = nil) {
        func control() -> XCUIElement {
            if let identifier {
                return app.buttons[identifier]
            }
            return app.buttons.matching(NSPredicate(format: "label == %@", label)).firstMatch
        }
        for _ in 0..<24 {
            let currentControl = control()
            if visibleInMaterialCatalog(app, currentControl) {
                currentControl.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
                return
            }
            swipeMaterialCatalog(app, upward: identifier == nil)
        }
        XCTFail("Could not reach Material catalog control: \(label)")
    }
    private func visibleInMaterialCatalog(_ app: XCUIApplication, _ element: XCUIElement) -> Bool {
        guard element.exists && element.isHittable else { return false }
        let scroll = app.descendants(matching: .any).matching(identifier: "material-catalog-scroll").firstMatch
        let viewport = scroll.exists ? scroll.frame.intersection(app.frame) : app.frame
        return !viewport.isNull && viewport.insetBy(dx: 2, dy: 2).contains(element.frame)
    }
    private func scrollToMaterialText(_ app: XCUIApplication, _ text: String) {
        let element = app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", text)).firstMatch
        for _ in 0..<24 {
            if visibleInMaterialCatalog(app, element) { return }
            swipeMaterialCatalog(app, upward: true)
        }
        XCTAssertTrue(visibleInMaterialCatalog(app, element), "Material catalog content is not visible: \(text)")
    }
    private func swipeMaterialCatalog(_ app: XCUIApplication, upward: Bool) {
        let scroll = app.descendants(matching: .any).matching(identifier: "material-catalog-scroll").firstMatch
        let target = scroll.exists ? scroll : app
        let x: CGFloat = scroll.exists ? 0.985 : 0.88
        let startY: CGFloat = upward ? 0.82 : 0.25
        let endY: CGFloat = upward ? 0.25 : 0.82
        let start = target.coordinate(withNormalizedOffset: CGVector(dx: x, dy: startY))
        let end = target.coordinate(withNormalizedOffset: CGVector(dx: x, dy: endY))
        start.press(forDuration: 0.05, thenDragTo: end)
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
        capture(app, "collection-measured", renderedText: "Collection layout: Measured column")
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
            capture(app, name, renderedText: "Collection layout: \(layout)")
        }
        ready(app, "Inventory drafts")
        let draft = app.textViews.matching(NSPredicate(format: "label == %@", "Item 1 draft")).firstMatch
        XCTAssertTrue(draft.waitForExistence(timeout: 10))
        draft.tap()
        draft.typeText("iOS table draft")
        // Match in XCTest's query engine; a Swift value getter can block its expectation timer.
        let edited = app.textViews.matching(NSPredicate(
            format: "label == %@ AND value CONTAINS %@", "Item 1 draft", "iOS table draft"
        )).firstMatch
        XCTAssertTrue(edited.waitForExistence(timeout: 10),
                      "Table draft value: \(String(describing: draft.value))")
    }

    func testProductComponents() {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Components")).firstMatch.tap()
        ready(app, "Component catalog")
        tapVisible(app, "More components")
        ready(app, "More shared components")
        tapVisible(app, "Documents and data lab")
        ready(app, "Documents and data lab")
        capture(app, "product-components", renderedText: "Documents and data lab")
    }

    func testMaterialCatalogSections() {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        ready(app, "Task workspace")
        app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Components")).firstMatch.tap()
        ready(app, "Component catalog")
        tapMaterialControl(app, "Open the full Material and expressive catalog")
        ready(app, "Material catalog")

        let sections = [
            ("Actions", "Choose an action", "material-actions"),
            ("Selection", "Receive updates", "material-selection"),
            ("Inputs", "January 1970", "material-inputs"),
            ("Surfaces", "Local surface override", "material-surfaces"),
            ("Navigation", "Home", "material-navigation"),
            ("Feedback", "No queued message", "material-feedback"),
            ("Gestures", "Swipe or use the dismissal action", "material-gestures"),
            ("Scaffold", "The app owns its state, injection and navigation.", "material-scaffold"),
        ]
        for (section, content, name) in sections {
            if section != "Actions" {
                tapMaterialControl(app, section, identifier: "material-section-\(section.lowercased())")
                let heading = app.descendants(matching: .any).matching(identifier: "material-section-heading").firstMatch
                XCTAssertTrue(heading.waitForExistence(timeout: 10), "Missing Material section heading after tapping \(section)")
                let selectedHeading = XCTNSPredicateExpectation(
                    predicate: NSPredicate(format: "label == %@", section),
                    object: heading,
                )
                XCTAssertEqual(
                    XCTWaiter.wait(for: [selectedHeading], timeout: 10),
                    .completed,
                    "Tapping \(section) did not switch the Material catalog route. Current heading: \(heading.label)",
                )
            }
            if section == "Actions" {
                ready(app, section)
            }
            scrollToMaterialText(app, content)
            capture(app, name, renderedText: content)
        }
    }

}
