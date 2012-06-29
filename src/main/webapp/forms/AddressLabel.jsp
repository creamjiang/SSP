<head>
<script src="http://code.jquery.com/jquery-latest.js"></script>



<style>
label {
	float: left;
	width: 120px;
	font-weight: bold;
}

input,textarea {
	width: 180px;
	margin-bottom: 5px;
}

textarea {
	width: 250px;
	height: 150px;
}

.boxes {
	width: 1em;
}

#submitbutton {
	margin-left: 120px;
	margin-top: 5px;
	width: 90px;
}

br {
	clear: left;
}
</style>




<script>
	function populateSpecialServices() {
		$.getJSON("/ssp/api/1/reference/specialServiceGroup/", function(data) {
			var container = $("#SpecialServiceGroupIds");

			$.each(data.rows, function(i, row) {
				addSelectItem(row.id, row.name, container);
			});

		}).error(function(jqXHR, textStatus, errorThrown) {
			alert(jqXHR + " " + textStatus + " " + errorThrown);
		});
	}

	function populateProgramStatus() {
		$.getJSON("/ssp/api/1/reference/programStatus/", function(data) {
			var container = $("#ProgramStatusGroup");

			$.each(data.rows, function(i, row) {
				addSelectItem(row.id, row.name, container);
			});

		}).error(function(jqXHR, textStatus, errorThrown) {
			alert(jqXHR + " " + textStatus + " " + errorThrown);
		});
	}

	function populateReferralSource() {
		$.getJSON("/ssp/api/1/reference/referralSource/", function(data) {
			var container = $("#ReferralSourceGroup");

			$.each(data.rows, function(i, row) {
				addSelectItem(row.id, row.name, container);
			});

		}).error(function(jqXHR, textStatus, errorThrown) {
			alert(jqXHR + " " + textStatus + " " + errorThrown);
		});
	}

	function populateStudentType() {
		$.getJSON("/ssp/api/1/reference/studentType/", function(data) {
			var container = $("#StudentTypeIds");

			$.each(data.rows, function(i, row) {
				addSelectItem(row.id, row.name, container);
			});

		}).error(function(jqXHR, textStatus, errorThrown) {
			alert(jqXHR + " " + textStatus + " " + errorThrown);
		});
	}

	function addSelectItem(uid, name, container) {
		var inputs = container.find('input');
		var id = inputs.length + 1;

		var html = '<option value="'+ uid +'">' + name + '</option>';
		container.append($(html));
	}

	
	
	
	$(document).ready(function() {
		populateSpecialServices();
		populateProgramStatus();
		populateReferralSource();
		populateStudentType();
	});	
	
</script>

</head>


<div class="AddressLabelForm">
	<h1>Address labels</h1>
	<form action="/ssp/api/1/report/AddressLabels/" method="get">
		<div class="box">
			<p>Address Label Report Criteria:</p>
			<p>required fields are denoted by an asterisc</p>
			<label><span>Program Status</span></label> <select
				id="ProgramStatusGroup" name="programStatus"
				class="custom-class1 custom-class2" style="width: 200px;">
				<option value=""></option>
				</select>
			<br /> <label><span>Student Type</span></label> <select
				id="StudentTypeIds" name="studentTypeIds" multiple="multiple"></select> 
			<br /> <label><span>Special Service Groups</span></label> <select
				id="SpecialServiceGroupIds" name="specialServiceGroupIds"
				multiple="multiple"></select> <br /> <label><span>Referral
					Source</span></label> <select id="ReferralSourceGroup" name="referralSourcesIds"
				multiple="multiple" /></select> <br />
			<label><span>Date Student Added From</span></label><input
				type="text" name="test1" id="dateAddedFrom" /><br /> <label><span>Date
					Student Added To</span></label><input type="text" name="test2" id="dateAddedTo" /><br />
			<label><span>Anticipated Start Term</span></label> <select
				id="anticipatedStartTerm" name="anticipatedStartTerm"
				class="custom-class1 custom-class2" style="width: 200px;">
				<option value=""></option>
				<option value="Fall" class="test-class-1">Fall</option>
				<option value="Winter" class="test-class-1">Winter</option>
				<option value="Spring" class="test-class-1">Spring</option>
				<option value="Summer" class="test-class-1">Summer</option>
			</select><br /> <label><span>Anticipated Start Year</span></label> <select
				id="anticipatedStartYear" name="anticipatedStartYear"
				class="custom-class1 custom-class2" style="width: 200px;">
                                <option value=""></option> 
				<option value="2010" class="test-class-1">2010</option>
				<option value="2011" class="test-class-1">2011</option>
				<option value="2012" class="test-class-1">2012</option>
				<option value="2013" class="test-class-1">2013</option>
				<option value="2014" class="test-class-1">2014</option>
				<option value="2015" class="test-class-1">2015</option>
				<option value="2016" class="test-class-1">2016</option>
				<option value="2017" class="test-class-1">2017</option>
				<option value="2018" class="test-class-1">2018</option>
				<option value="2019" class="test-class-1">2019</option>
				<option value="2020" class="test-class-1">2020</option>
			</select><br /> <input type="submit" />
		</div>
	</form>
</div>










