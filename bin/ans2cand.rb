ss=readlines
s=ss.join("")
s.gsub!(/.*cover:\n/m,"")
puts s
