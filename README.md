Run
`java -jar poly.jar`

Links:
------
+ [ポリオミノのはみ出し可能な被覆問題](http://www.alg.cei.uec.ac.jp/itohiro/Games/Game100301.html#anchor3)
+ [稲葉のパズル研究室：1×5が覆えない](http://inabapuzzle.com/hirameki/suuri_7.html)
+ [mixi: ポリオミノを覆えないポリオミノを探す問題（仮題）](http://mixi.jp/view_bbs.pl?comm_id=3271312&id=49259367)
+ [Math puzzle (I Pentomino Exclusion)](http://www.mathpuzzle.com/23Dec2010.html)

ファイル形式
----------
問題とその解のライブラリは、
problem/ と ans/ 以下にある。problem 以下には問題ファイルがあり、その解決状況は拡張子で表されている。
problem/path/to/foo.yes には解が見つかっており、ans/path/to/foo.ans または ans/path/to/foo.dup がその解である。
拡張子 .dup は全く同一の .ans ファイルが存在することを意味する。
problem/path/to/foo.no は未解決問題。
problem/path/to/foo.meh は、それのサブセットにすでに解が見つかっていることを意味する。

ui.Main で起動されるUIで、file を load した時の挙動について：
covered:
cover:
のどちらかが含まれる行を読むと、その後をそれぞれ問題、解候補として読み込む。1つのファイルに問題、解候補が複数含まれていても良い。
どちらもなく、拡張子が .ans または .dup の場合は、それを解候補として読み込む。
拡張子が .ans または .dup 以外の場合は、それを問題として読み込む。

Scripts
-------
+ To update poly.jar, run:
`./run.sh`

+ `src/bit2poly.rb`: 一行に 0100 1111 (この場合はYペントミノ) などと表されているファイルを標準入力から読み込み、指定したディレクトリ以下に本プログラムの形式でファイルを作成する。拡張子は.no となる。

+ `src/no2meh.sh`: .no のファイルのなかで、実は .meh であるものを列挙し、拡張子を .meh に変える。

+ `src/searchall.sh` 例えば、`src/searchall.sh --min_num_cand=2 --max_num_cand=3 problem/hexomino ans 2> /dev/null`
とすると、cand を2枚以上、3枚以下使うという条件のもとで、problem/hexomino 以下にある拡張子が .no の問題と、ans 以下にあるすべてのポリオミノとの総当りを行い、どれだけ深く刺すのを許した時におおえてしまうかをすべてのペアに対して出力する。
