
var tabCount = 0;
var data;

const form = {
	query: document.getElementById('search-query'),
	submit: document.getElementById('search-button')

};

const ulSearchRes = document.getElementById('search-res');
const paginationTabs = document.getElementById('pagination-tabs');

console.log(form.query.value);

function clearOldData() {
	console.log('Clearing old data. . .');
	while(ulSearchRes.firstChild) {
		ulSearchRes.removeChild(ulSearchRes.firstChild);
	}

	while(paginationTabs.firstChild) {
		paginationTabs.removeChild(paginationTabs.firstChild);
	}
}

function clearOldResults() {
	while(ulSearchRes.firstChild) {
		ulSearchRes.removeChild(ulSearchRes.firstChild);
	}
}

function activateTab(tabID) {
	for(var i = 1; i <= tabCount; i++) {
		document.getElementById('tab-' + i).classList.remove("active");
	}

	document.getElementById(tabID).classList.add("active");
}

function paginationHandler() {
	var tabID = this.id;
	
	activateTab(tabID);
	clearOldResults();
	displayPaginationTabResults((tabID.split('-'))[1]);
}

function createPaginationTab(tabID) {
	console.log('Create Pagination Tab' + tabID);
	const a = document.createElement('a');
	a.href = '#';
	if(tabID == 1) {
		a.classList.add('active');
	}
	a.innerHTML = tabID;
	a.id = "tab-" + tabID;
	paginationTabs.appendChild(a);
}



function displayPaginationTabResults(tabID) {
	var i = tabID-1;
	for(var j = 0; j < 10; j++) {
		if((10*i + j) == data.length)
			break;

		var res = data[10*i + j];
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
	}
	//activateTab(tabID, Math.ceil(data.length / 10));
	document.getElementById('pagination-div').style.display = 'block';
}

function displayNoResultsMessage() {
  	console.log('no results');

	const li = document.createElement('li');
	li.classList.add('mdc-list-item');
	li.classList.add('mdc-ripple-upgraded');


	const span1 = document.createElement('span');
	span1.classList.add('mdc-list-item__text');

	const span2 = document.createElement('span');
	//span2.classList.add('mdc-list-item__primary-text');
	span2.classList.add('no-results');
	span2.innerHTML = 'No results found for this query';

	
	span1.appendChild(span2);
	li.appendChild(span1);
	
	ulSearchRes.appendChild(li);
	document.getElementById('search-results').style.display = 'block';
}

function setOnClickListenerOnTabs() {
	var tabCount = Math.ceil(data.length / 10);
	for(var tabID = 1; tabID <= tabCount; tabID++) {
		document.getElementById('tab-' + tabID).addEventListener('click', paginationHandler);
	}
}

function getResults() {
	var request = new XMLHttpRequest();
	var url = 'http://localhost:8080/query?tokens=' + form.query.value;
	request.open('GET', url);


	request.onload = function () {
		data = JSON.parse(this.response);
		
		if (request.status >= 200 && request.status < 400) {
			clearOldData();

			tabCount = Math.ceil(data.length / 10);
			
			for(var i = 0; i < tabCount; i++) {
				createPaginationTab(i+1);
			}

			setOnClickListenerOnTabs();

			if(data.length > 0){
				displayPaginationTabResults(1, data);
			}

		  	console.log(data.length);
		  	if(data.length == 0) {
		  		displayNoResultsMessage();
		  	}
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
	
