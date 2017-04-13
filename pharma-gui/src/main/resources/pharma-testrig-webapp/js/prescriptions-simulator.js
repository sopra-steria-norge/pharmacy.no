function dateString(date) {
	return date && date.toISOString().substring(0, 10);
}
function randomDaysAgo(maxDays) {
	return new Date(new Date() - Math.random() * maxDays*24*60*60*1000);	
}

function submitJsonPost(url, json) {
	var form = document.createElement("form");
	form.setAttribute("method", "POST");
	form.setAttribute("action", url);
	
	var dataField = document.createElement("input");
	dataField.setAttribute("type", "hidden");
	dataField.setAttribute("name", "JSON");
	dataField.setAttribute("value", JSON.stringify(json));
	form.append(dataField);
	
	document.body.append(form);
	form.submit();
}


var ListSelect = Vue.component('list-select', {
	template: `<div>
			<p>
				<slot name="selected" :selected="value" v-if="value">{{value}}</slot>
				<a href="#" @click="toggle">[endre]</a>
			</p>
			<ul v-if="displayOptions">
				<p>
					<input v-model="itemFilter" placeholder="søk" autofocus />
				</p>
				<li v-for="option in options"><a href="#" @click="selectOption(option)"
					>[ velg ]</a><slot :option="option">{{option}}</slot></li>
			</ul>
		</div>`,
	model: {
		prop: 'value',
		event: 'input'
	},
	props: {
		"allOptions": {type: Array, required:true},
		"value": [Object, String],
		"filter": {type: Function},
		"objectFilter": {type: Function}
	},
	data: function () {
		return {
			displayOptions: false,
			itemFilter: ""
		}; 
	},
	methods: {
		toggle: function() {
			this.displayOptions = !this.displayOptions;
		},
		selectOption: function(option) {
			this.displayOptions = false;
			this.currentOption = option;
			this.itemFilter = "";
			this.$emit('input', option);
		}
	},
	computed: {
		options: function() {
			return this.allOptions.filter(this.objectFilter(this.itemFilter)).slice(0, 10);
		}
	}
});




function filterPatients(string) {
	if (string.match(/^\d+$/)) {
		return function(o) {
			return o.nationalId.indexOf(string) == 0;
		};
	}
	const filter = string.toUpperCase();
	return function(o) {
		var fullName = (o.firstName + " " + o.lastName).toUpperCase();
		var lastFirst = (o.lastName + ", " + o.firstName).toUpperCase();
		return fullName.indexOf(filter) == 0 || 
			lastFirst.toUpperCase().indexOf(filter) == 0;
	};
};


function filterPractitioners(string) {
	if (string.length < 3) {
		return function(o) { return true; };
	}
	const filter = string.toUpperCase();
	if (isNaN(filter)) {
		return function(o) {
			return o.name.toUpperCase().includes(filter);
		};
	} else {
		return function(o) {
			return o.id.indexOf(filter) == 0;
		};
	}
};

function filterMedications(string) {
	if (string.length < 2) {
		return function(o) { return true; };
	}
	const filter = string.toUpperCase();
	if (filter.match(/^[A-Z]\d+([A-Z]([A-Z]\d{0,2})?)?$/)) {
		return function(o) {
			return o.atc && o.atc.indexOf(filter) == 0;
		};
	} else if (filter.match(/^\d+$/)) {
		return function(o) {
			return o.productId.indexOf(filter) == 0;
		}
	} else {
		return function(o) {
			return o.display.toUpperCase().indexOf(filter) == 0;
		};
	}
};

