package main

import "fmt"
import "flag"
import "path/filepath"
import "os"
import "os/exec"
import "strings"
import "time"
import "bufio"

var port = flag.String("port", "1829", "number of port to use for Elda")
var clean = flag.Bool("clean", false, "set true to clean out caches")
var jar = flag.Bool("jar", false, "set true to build Elda current jar")
var rebuild = flag.Bool("rebuild", false, "set true to rebuild historical data")

func main() {
	setup()
	for _, elda := range find("eldas") {
		for _, config_file := range find("configs") {
			fmt.Println(";;; elda", elda, "& configuration", config_file)
			generateBaseData(elda, config_file)
		}
	}
}

// me=current-elda
//
//for config_file in configs/*.ttl; do
//	echo
//	echo ";;; configuration" ${config_file} "------------------------------------------------------"
//	echo
//	export config=$(echo $config_file | sed -e 's:.*/::' -e 's/\.ttl$//')
//	export c_name=${config}
//	java -jar this-elda/* -Djetty.port=${_PORT} -Delda.spec=$PWD/$config_file &>> servers.log &
//	sleep 10
//#
//	for uri in $(cat uris/$config); do
//		export r_name=elda-${e_name}_${config}_${u_name}
//		export u_name=$(echo $uri | sed -e 's/[^A-Za-z0-9._\-]\+/_/g')
//		wget -O results/${r_name} http://localhost:${_PORT}/$uri &> clients.log
//		for elda in eldas/*.jar; do
//			export o_name=$(echo $elda | sed -e 's/.*-standalone-//' -e 's/.jar//')
//			export d_file=${e_name}_vs_${o_name}_${c_name}_${u_name}
//			echo CONSIDERING ${d_file}
//			if diff results/${r_name} results/elda-${o_name}_${c_name}_${u_name} > differences/${d_file}; then
//				echo OK -- no differences
//			else
//				if [ -e gold_differences/${d_file} ]; then
//					if diff differences/${d_file} gold_differences/${d_file} > meta_differences/${d_file} ; then
//						echo OK -- the same differences as last time
//					else
//						echo "PROBLEM -- different differences, there's been a change. Oops."
//					fi
//				else
//					echo "OK (ish) -- no existing differences to compare with for" ${d_file}
//					cp differences/${d_file} gold_differences/${d_file}
//				fi
//			fi
//		done
//	done
//	pkill -f elda.spec


func generateBaseData(elda, config_file string) {
	config := config_file[strings.LastIndex(config_file, "/")+1 : len(config_file)-4]
	e_name := elda[strings.Index(elda, "standalone")+11 : len(elda)-4]
	fmt.Println("elda:", e_name, " & config:", config)
	absConfig, _ := filepath.Abs(config_file)
	cmd := exec.Command("java", "-jar", elda, "-Djetty.port="+*port, "-Delda.spec="+absConfig )
	err := cmd.Start()
	if err != nil {
		panic(err)
	}
	if true {
		fmt.Println("Giving Elda a chance to get started ...")
		time.Sleep(10 * time.Second)
	}
	//
	reader, _ := os.Open("uris/" + config)
	scanner := bufio.NewScanner(reader)
	for scanner.Scan() {
		uri := scanner.Text()
		u_name := cleanURI(uri)
		r_name := "elda-" + e_name + "_" + config + "_" + u_name
		_ = uri + u_name + r_name
		fmt.Println("wget", "-O", "results/"+r_name, "http://localhost:"+*port+"/"+uri)
		cmd := exec.Command("wget", "-O", "results/"+r_name, "http://localhost:"+*port+"/"+uri)
		output, err := cmd.CombinedOutput()
		if err != nil {
			fmt.Println("OOPS", err)
			fmt.Println(string(output))
		}
	}
	//
	exec.Command("pkill", "-f", "elda.spec").Run()
}

func cleanURI(uri string) string {
	bytes := make([]byte, 0, len(uri))
	for _, ch := range uri {
		if 'A' <= ch && ch <= 'Z' || 'a' <= ch && ch <= 'z' || ch == '-' || ch == '.' || ch == '_' {
			// nothing to do
		} else {
			ch = '_'
		}
		bytes = append(bytes, byte(ch))
	}
	return string(bytes)
}

func find(path string) (paths []string) {
	filepath.Walk(path, func(path string, info os.FileInfo, err error) error {
		if info.IsDir() == false {
			paths = append(paths, path)
		}
		return nil
	})
	return paths
}

func setup() {
	flag.Parse()
	fmt.Println("port:   ", *port)
	fmt.Println("clean:  ", *clean)
	fmt.Println("jar:    ", *jar)
	fmt.Println("rebuild:", *rebuild)
}

//export _PORT=1829
//export _JAR=yes
//export _REBUILD=yes
//export _CLEAN=no
//
//for x in $* ; do
//	export _$x
//done
//
//echo
//echo ";;; Build current jar: " $_JAR
//echo ";;; Elda port:         " $_PORT
//echo ";;; Rebuild outputs:   " $_REBUILD
//echo ";;; Clean first:       " $_CLEAN
//
//mkdir -p this-elda
//mkdir -p gold results differences meta_differences gold_differences
//
//if [ ${_CLEAN} == "yes" ]; then
//	rm -rf gold/*
//	rm -rf results/*
//	rm -rf differences/*
//	rm -rf gold_differences/*
//	rm -rf meta_differences/*
//fi
//
//if [ $_JAR == "yes" ]; then
//	echo "Building current (probably snapshot) jar"
//	(cd ..; mvn clean package)
//	mv ../elda-standalone/target/elda-standalone-*.jar this-elda
//fi
//
//if [ $_REBUILD == "yes" ]; then
//	echo
//	echo ";;; preparing golden results and differences ================================================"
//	echo
//#
//	for elda in eldas/*.jar
//	do
//		for config_file in configs/*.ttl
//		do
//			echo
//			echo ";;; configuration" ${config_file} "-----------------------------------------------"
//			echo
//			export config=$(echo $config_file | sed -e 's:.*/::' -e 's/\.ttl$//')
//			export e_name=$(echo $elda | sed -e 's/.*-standalone-//' -e 's/.jar//')
//			export c_name=$config
//		#
//			echo ";;; -- java -jar $elda -Delda.spec=$PWD/$config_file -----------------------------"
//			java -jar $elda -Djetty.port=${_PORT} -Delda.spec=$PWD/$config_file &> servers.log &
//			# sleep to allow the server time to initialise
//			sleep 10
//		#
//			for uri in $(cat uris/$config)
//			do
//				export u_name=$(echo $uri | sed -e 's/[^A-Za-z0-9._\-]\+/_/g')
//				export r_name=elda-${e_name}_${c_name}_${u_name}
//				export g_name=${c_name}_${u_name}
//			#
//				echo ";;; result filename: $r_name"
//				echo ";;;;; -- wget http://localhost:8080/$uri"
//				wget -O results/${r_name} http://localhost:${_PORT}/$uri
//			done
//			pkill -f elda.spec
//		done
//	done
//fi
//
//echo
//echo ";;; generating results for current elda ----------------------------------------------------"
//echo
//
//export e_name=current-elda
//
//for config_file in configs/*.ttl; do
//	echo
//	echo ";;; configuration" ${config_file} "------------------------------------------------------"
//	echo
//	export config=$(echo $config_file | sed -e 's:.*/::' -e 's/\.ttl$//')
//	export c_name=${config}
//	java -jar this-elda/* -Djetty.port=${_PORT} -Delda.spec=$PWD/$config_file &>> servers.log &
//	sleep 10
//#
//	for uri in $(cat uris/$config); do
//		export r_name=elda-${e_name}_${config}_${u_name}
//		export u_name=$(echo $uri | sed -e 's/[^A-Za-z0-9._\-]\+/_/g')
//		wget -O results/${r_name} http://localhost:${_PORT}/$uri &> clients.log
//		for elda in eldas/*.jar; do
//			export o_name=$(echo $elda | sed -e 's/.*-standalone-//' -e 's/.jar//')
//			export d_file=${e_name}_vs_${o_name}_${c_name}_${u_name}
//			echo CONSIDERING ${d_file}
//			if diff results/${r_name} results/elda-${o_name}_${c_name}_${u_name} > differences/${d_file}; then
//				echo OK -- no differences
//			else
//				if [ -e gold_differences/${d_file} ]; then
//					if diff differences/${d_file} gold_differences/${d_file} > meta_differences/${d_file} ; then
//						echo OK -- the same differences as last time
//					else
//						echo "PROBLEM -- different differences, there's been a change. Oops."
//					fi
//				else
//					echo "OK (ish) -- no existing differences to compare with for" ${d_file}
//					cp differences/${d_file} gold_differences/${d_file}
//				fi
//			fi
//		done
//	done
//	pkill -f elda.spec
//done
