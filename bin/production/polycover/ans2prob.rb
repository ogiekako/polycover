ss=readlines
s=ss.join("")
s.gsub!(/.*covered:\n/m,"")
s.gsub!(/cover:.*/m,"")
puts s
