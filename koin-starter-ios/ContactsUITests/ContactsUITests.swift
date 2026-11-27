import XCTest

final class ContactsUITests: XCTestCase {
    func testContactIsVisible() {
        let app = XCUIApplication()
        app.launch()
        let title = app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", "Alex")).firstMatch
        XCTAssertTrue(title.waitForExistence(timeout: 30))
    }
}
