import urllib.request
import urllib.parse


def main():
	urls = set()
	urls.add('https://www.cs.uic.edu/')
	crawl(urls)

def crawl(urls):
	for url in urls:
		if uicDomain(url):
			print(url)
			f = urllib.request.urlopen(url)
			connection.request('GET', '/2.0/repositories')
			print(f.read().decode('utf-8'))
			


def uicDomain(url):
	p1 = url.split('/')
	if p1[0].startswith('http'):
		if p1[2].endswith('uic.edu'):
			return True
	else:
		if p1[0].endswith('uic.edu'):
			return True
	return False


if __name__ == '__main__':
	main()