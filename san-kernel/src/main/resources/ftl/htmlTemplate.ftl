<#ftl>
<#compress>
<html>
<head>
 <style type="text/css">
    .divTable{
	display: table;
	width: 100%;
}
.divTableRow {
	display: table-row;
}
.divTableHeading {
	background-color: #EEE;
	display: table-header-group;
}
.divTableCell, .divTableHead {
	border: 1px solid #999999;
	display: table-cell;
	padding: 3px 10px;
}
.divTableHeading {
	background-color: #EEE;
	display: table-header-group;
	font-weight: bold;
}
.divTableFoot {
	background-color: #EEE;
	display: table-footer-group;
	font-weight: bold;
}
.divTableBody {
	display: table-row-group;
}
    </style>
</head>
<body>

<div class="divTable" style="width: 100%;border: 1px solid #000;" >
<div class="divTableHeading" >
	<div class="divTableCell">id</div>
	<div class="divTableCell">number</div>
	<div class="divTableCell">status</div>
	<div class="divTableCell">suggested</div>
</div>
<div class="divTableBody">
<#list ROWS as row >
	<div class="divTableRow">
		<div class="divTableCell">${row.id}</div>
		<div class="divTableCell">${row.number}</div>
		<div class="divTableCell">${row.status}</div>
		<#if row.suggested?? >
			<div class="divTableCell">${row.suggested}</div>
		</#if>
	</div>
</#list>
</div>
</div>
</div>
</body>
</html>
</#compress>