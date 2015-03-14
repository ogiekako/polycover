#!ruby
# Usage: cat 4-9/7.txt | src/bit2poly.rb problem/7
if ARGV[0] == nil then 
    puts "Usage: cat input | src/bit2poly.rb outputDir"
    exit 1
end
STDIN.each {|line|
    next if line.include?("-")
    ss = line.split(" ")
    name = ss.join("_")
    path = ARGV[0] + "/" + name + ".no"
    h = ss.size
    w = ss[0].size
    f = open(path, "w")
    f.puts "#{h} #{w}"
    f.puts ss.join("\n").gsub("0",".").gsub("1","#")
}
