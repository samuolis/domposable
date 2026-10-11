import SwiftUI
import DomposableDemo

struct ComposeContent: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.mainViewController()
    }
    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}

@main
struct DomposableDemoApp: App {
    var body: some Scene {
        WindowGroup { ComposeContent().ignoresSafeArea(.keyboard) }
    }
}
