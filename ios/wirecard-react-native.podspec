
Pod::Spec.new do |s|
  s.name         = "wirecard-react-native"
  s.version      = "2.0.0-rc5"
  s.summary      = 'lightweight react-native wrapper for wirecard'
  s.description  = "lightweight react-native wrapper for wirecard use wire card in react-native"
  s.homepage     = "www.uft.lu"
  s.license      = "MIT"
  # s.license      = "MIT"
  s.author       = "Joao.r.silverio@gmail.com"
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/author/RNWirecard.git", :tag => "master" }
  s.source_files  = "*.{h,m}"
  s.static_framework = true
  s.requires_arc = true


  s.dependency 'paymentSDK/All'
  s.dependency 'React'
  #s.dependency "others"

end

 