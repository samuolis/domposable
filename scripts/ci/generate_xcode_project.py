#!/usr/bin/env python3
"""Deterministic small Xcode project; no generator installation required."""
from pathlib import Path
import hashlib
import json
import sys
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from version import read_version

ROOT = Path(__file__).resolve().parents[2]
release_version, release_build = read_version(ROOT / "version.properties")
objects = {}
def ident(name):
    return hashlib.sha256(name.encode()).hexdigest()[:24].upper()
def add(name, fields):
    objects[ident(name)] = fields
    return ident(name)
def ref(name):
    return ident(name)

app_file = add("app-file", {"isa":"PBXFileReference","lastKnownFileType":"sourcecode.swift","path":"DomposableDemo/App.swift","sourceTree":"<group>"})
test_file = add("test-file", {"isa":"PBXFileReference","lastKnownFileType":"sourcecode.swift","path":"DomposableDemoUITests/SmokeTests.swift","sourceTree":"<group>"})
app_product = add("app-product", {"isa":"PBXFileReference","explicitFileType":"wrapper.application","path":"DomposableDemo.app","sourceTree":"BUILT_PRODUCTS_DIR"})
test_product = add("test-product", {"isa":"PBXFileReference","explicitFileType":"wrapper.cfbundle","path":"DomposableDemoUITests.xctest","sourceTree":"BUILT_PRODUCTS_DIR"})
for name, file in [("app",app_file),("test",test_file)]:
    build_file=add(name+"-build-file", {"isa":"PBXBuildFile","fileRef":file})
    add(name+"-sources", {"isa":"PBXSourcesBuildPhase","buildActionMask":"2147483647","files":[build_file],"runOnlyForDeploymentPostprocessing":"0"})
    add(name+"-frameworks", {"isa":"PBXFrameworksBuildPhase","buildActionMask":"2147483647","files":[],"runOnlyForDeploymentPostprocessing":"0"})
    configs=[]
    for config in ["Debug","Release"]:
        settings={
            "PRODUCT_NAME":"$(TARGET_NAME)","SWIFT_VERSION":"5.0","IPHONEOS_DEPLOYMENT_TARGET":"15.0",
            "TARGETED_DEVICE_FAMILY":"1,2","ARCHS":"arm64","ONLY_ACTIVE_ARCH":"YES","GENERATE_INFOPLIST_FILE":"YES","SDKROOT":"iphoneos",
            "SUPPORTED_PLATFORMS":"iphoneos iphonesimulator","CODE_SIGN_STYLE":"Automatic",
            "PRODUCT_BUNDLE_IDENTIFIER":"io.github.samuolis.domposable.demo"+(".uitests" if name=="test" else ""),
            "MARKETING_VERSION":"$(DOMPOSABLE_MARKETING_VERSION)","CURRENT_PROJECT_VERSION":"$(DOMPOSABLE_BUILD_NUMBER)",
            "DOMPOSABLE_MARKETING_VERSION":f"{release_version.major}.{release_version.minor}.{release_version.patch}","DOMPOSABLE_BUILD_NUMBER":str(max(1, release_build)),
            "SWIFT_OPTIMIZATION_LEVEL":"-Onone" if config=="Debug" else "-O",
            "DEBUG_INFORMATION_FORMAT":"dwarf" if config=="Debug" else "dwarf-with-dsym",
        }
        if name=="app":settings.update({
            "INFOPLIST_FILE":"DomposableDemo/Info.plist",
            "INFOPLIST_KEY_UILaunchScreen_Generation":"YES","INFOPLIST_KEY_UIApplicationSceneManifest_Generation":"YES",
            "INFOPLIST_KEY_CFBundleDisplayName":"Domposable",
            "INFOPLIST_KEY_CADisableMinimumFrameDurationOnPhone":"YES",
            "PRODUCT_MODULE_NAME":"DomposableSample",
            "KOTLIN_TARGET":"iosArm64","KOTLIN_TARGET[sdk=iphonesimulator*]":"iosSimulatorArm64",
            "KOTLIN_BUILD_TYPE":"debug" if config=="Debug" else "release",
            "FRAMEWORK_SEARCH_PATHS":"$(inherited) $(SRCROOT)/../demo/build/bin/$(KOTLIN_TARGET)/$(KOTLIN_BUILD_TYPE)Framework",
            "OTHER_LDFLAGS":"$(inherited) -framework DomposableDemo",
        })
        else:settings["TEST_TARGET_NAME"]="DomposableDemo"
        configs.append(add(name+"-"+config,{"isa":"XCBuildConfiguration","name":config,"buildSettings":settings}))
    add(name+"-config-list", {"isa":"XCConfigurationList","buildConfigurations":configs,"defaultConfigurationIsVisible":"0","defaultConfigurationName":"Release"})
    add(name+"-target", {"isa":"PBXNativeTarget","name":"DomposableDemo"+("UITests" if name=="test" else ""),"productName":"DomposableDemo"+("UITests" if name=="test" else ""),"productType":"com.apple.product-type.application" if name=="app" else "com.apple.product-type.bundle.ui-testing","productReference":app_product if name=="app" else test_product,"buildConfigurationList":ref(name+"-config-list"),"buildPhases":[ref(name+"-sources"),ref(name+"-frameworks")],"buildRules":[],"dependencies":[]})
products=add("products", {"isa":"PBXGroup","name":"Products","children":[app_product,test_product],"sourceTree":"<group>"})
group=add("main-group", {"isa":"PBXGroup","children":[app_file,test_file,products],"sourceTree":"<group>"})
configs=[add("project-"+name,{"isa":"XCBuildConfiguration","name":name,"buildSettings":{"CLANG_ENABLE_MODULES":"YES"}}) for name in ["Debug","Release"]]
add("project-configs", {"isa":"XCConfigurationList","buildConfigurations":configs,"defaultConfigurationIsVisible":"0","defaultConfigurationName":"Release"})
add("project", {"isa":"PBXProject","attributes":{"LastUpgradeCheck":"2660","TargetAttributes":{ref("test-target"):{"TestTargetID":ref("app-target")}}},"buildConfigurationList":ref("project-configs"),"compatibilityVersion":"Xcode 14.0","developmentRegion":"en","hasScannedForEncodings":"0","knownRegions":["en","Base"],"mainGroup":group,"productRefGroup":products,"projectDirPath":"","projectRoot":"","targets":[ref("app-target"),ref("test-target")]})
def encode(value):
    if isinstance(value,dict):return "{ " + " ".join(f"{json.dumps(key)} = {encode(item)};" for key,item in value.items()) + " }"
    if isinstance(value,list):return "("+", ".join(encode(item) for item in value)+(",)" if value else ")")
    return json.dumps(value)
project=ROOT/"iosApp/DomposableDemo.xcodeproj"
project.mkdir(parents=True,exist_ok=True)
(project/"project.pbxproj").write_text("// !$*UTF8*$!\n"+encode({"archiveVersion":"1","classes":{},"objectVersion":"56","objects":objects,"rootObject":ref("project")}))
schemes=project/"xcshareddata/xcschemes";schemes.mkdir(parents=True,exist_ok=True)
def buildable(name):
    return f'<BuildableReference BuildableIdentifier="primary" BlueprintIdentifier="{ref(name+"-target")}" BuildableName="DomposableDemo{ "UITests.xctest" if name=="test" else ".app" }" BlueprintName="DomposableDemo{ "UITests" if name=="test" else "" }" ReferencedContainer="container:DomposableDemo.xcodeproj"/>'
(schemes/"DomposableDemo.xcscheme").write_text(f'''<?xml version="1.0" encoding="UTF-8"?>
<Scheme LastUpgradeVersion="2660" version="1.3">
 <BuildAction parallelizeBuildables="YES" buildImplicitDependencies="YES"><BuildActionEntries>
  <BuildActionEntry buildForTesting="YES" buildForRunning="YES" buildForProfiling="YES" buildForArchiving="YES" buildForAnalyzing="YES">{buildable("app")}</BuildActionEntry>
 </BuildActionEntries></BuildAction>
 <TestAction buildConfiguration="Debug" selectedDebuggerIdentifier="Xcode.DebuggerFoundation.Debugger.LLDB" selectedLauncherIdentifier="Xcode.IDEFoundation.Launcher.LLDB" shouldUseLaunchSchemeArgsEnv="YES"><Testables><TestableReference skipped="NO">{buildable("test")}</TestableReference></Testables></TestAction>
 <LaunchAction buildConfiguration="Debug" selectedDebuggerIdentifier="Xcode.DebuggerFoundation.Debugger.LLDB" selectedLauncherIdentifier="Xcode.IDEFoundation.Launcher.LLDB" launchStyle="0" useCustomWorkingDirectory="NO" ignoresPersistentStateOnLaunch="NO" debugDocumentVersioning="YES" debugServiceExtension="internal" allowLocationSimulation="YES"><BuildableProductRunnable runnableDebuggingMode="0">{buildable("app")}</BuildableProductRunnable></LaunchAction>
 <ProfileAction buildConfiguration="Release" shouldUseLaunchSchemeArgsEnv="YES" savedToolIdentifier="" useCustomWorkingDirectory="NO" debugDocumentVersioning="YES"><BuildableProductRunnable runnableDebuggingMode="0">{buildable("app")}</BuildableProductRunnable></ProfileAction>
 <AnalyzeAction buildConfiguration="Debug"/><ArchiveAction buildConfiguration="Release" revealArchiveInOrganizer="YES"/>
</Scheme>''')
