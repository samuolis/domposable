import SwiftUI
import KoinStarter

struct ContactsView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.mainViewController()
    }

    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}

@main
struct ContactsApp: App {
    var body: some Scene {
        WindowGroup { ContactsView().ignoresSafeArea(.keyboard) }
    }
}
