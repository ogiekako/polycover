Run
`java -jar poly.jar`

Links:
+ ポリオミノのはみ出し可能な被覆問題
http://www.alg.cei.uec.ac.jp/itohiro/Games/Game100301.html#anchor3
+ 稲葉のパズル研究室：1×5が覆えない
http://inabapuzzle.com/hirameki/suuri_7.html
+ mixi: ポリオミノを覆えないポリオミノを探す問題（仮題）
http://mixi.jp/view_bbs.pl?comm_id=3271312&id=49259367

To update poly.jar, run:
`./run.sh`

問題とその解のライブラリは、
problem/ と ans/ 以下にある。problem 以下には問題ファイルがあり、その解決状況は拡張子で表されている。
problem/path/to/foo.yes には解が見つかっており、ans/path/to/foo.ans がその解である。
problem/path/to/foo.no は未解決問題。
problem/path/to/foo.yeah は、それのサブセットにすでに解が見つかっていることを意味する。