const form = {
	query: document.getElementById('search-query'),
	submit: document.getElementById('search-button')

};

const ulSearchRes = document.getElementById('search-res');

console.log(form.query.value);

function getResults() {
	var request = new XMLHttpRequest();
	var url = 'http://localhost:8080/query?tokens=' + form.query.value;
	request.open('GET', url);


	request.onload = function () {
		console.log('Called onload')
		// Begin accessing JSON data here
		var data = JSON.parse(this.response)
		if (request.status >= 200 && request.status < 400) {
			while(ulSearchRes.firstChild) {
					ulSearchRes.removeChild(ulSearchRes.firstChild);
			}

	  		data.forEach(res => {
		    	console.log(res.title);
		    	console.log(res.excerpt);
				console.log(res.url);

          		const a = document.createElement('a');
          		a.href = res.url;

				const li = document.createElement('li');
				li.classList.add('mdc-list-item');
				li.classList.add('mdc-ripple-upgraded');


				const span1 = document.createElement('span');
				span1.classList.add('mdc-list-item__text');

				const span2 = document.createElement('span');
				span2.classList.add('mdc-list-item__primary-text');
				span2.innerHTML = res.title;

				const span3 = document.createElement('span');
				span3.classList.add('mdc-list-item__secondary-text');
				span3.innerHTML = res.excerpt;

				span1.appendChild(span2);
				span1.appendChild(span3);
				li.appendChild(span1);
				a.appendChild(li);

				ulSearchRes.appendChild(a);
				document.getElementById('search-results').style.display = 'block';

	  	})
		} else {
	  		console.log('error')
		}
	};

	request.send(null);

}

form.submit.addEventListener('click', getResults);


if(form.query.value) {
    console.log('query not empty');
    window.onload = getResults;
}
	
