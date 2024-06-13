<?php
// Report all PHP errors
error_reporting(E_ALL);


require_once('./include/Flug.php');
require_once('./include/FlugResult.php');
require_once('./include/FlugService.php');

$flugService = new FlugService();

$flugResult = $flugService->filterFluege();
$fluege = $flugResult->fluege;

?>
<!doctype html>
<html lang="de">

<head>
	<!-- Required meta tags -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<!--favicon-->
	<link rel="icon" href="assets/images/favicon-32x32.png" type="image/png" />
	<!--plugins-->
	<link href="assets/plugins/simplebar/css/simplebar.css" rel="stylesheet" />
	<link href="assets/plugins/perfect-scrollbar/css/perfect-scrollbar.css" rel="stylesheet" />
	<link href="assets/plugins/metismenu/css/metisMenu.min.css" rel="stylesheet" />
	<link href="assets/plugins/datatable/css/dataTables.bootstrap5.min.css" rel="stylesheet" />
	<link rel="stylesheet" href="assets/plugins/notifications/css/lobibox.min.css" />
	<!-- loader-->
	<link href="assets/css/pace.min.css" rel="stylesheet" />
	<script src="assets/js/pace.min.js"></script>
	<!-- Bootstrap CSS -->
	<link href="assets/css/bootstrap.min.css" rel="stylesheet">
	<link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500&display=swap" rel="stylesheet">
	<link href="assets/css/app.css" rel="stylesheet">
	<link href="assets/css/icons.css" rel="stylesheet">

	<title>Flugbuch</title>
</head>

<body class="bg-theme bg-theme1">
	<!--wrapper-->
	<div class="wrapper">
		<!--sidebar wrapper -->
		<div class="sidebar-wrapper" data-simplebar="true">
			<div class="sidebar-header">
				<div>
					<img src="assets/images/logo-icon.png" class="logo-icon" alt="logo icon">
				</div>
				<div>
					<h4 class="logo-text">Hauptflugbuch</h4>
				</div>
				
			</div>
			<!--navigation-->
			<?php include('inc/navigation.php') ?>
			<!--end navigation-->
		</div>
		<!--end sidebar wrapper -->
		<!--start header -->
		<?php include('inc/header.php') ?>
		<!--end header -->
		<!--start page wrapper -->
		<div class="page-wrapper">
			<div class="page-content">
				<!--breadcrumb-->
				<div class="page-breadcrumb d-none d-sm-flex align-items-center mb-3">
					<div class="breadcrumb-title pe-3">Flugbuch</div>
					<div class="ps-3">
						<nav aria-label="breadcrumb">
							<ol class="breadcrumb mb-0 p-0">
								<li class="breadcrumb-item"><a href="javascript:;"><i class="bx bx-home-alt"></i></a>
								</li>
								<li class="breadcrumb-item active" aria-current="page">Flüge</li>
							</ol>
						</nav>
					</div>
					<!--<div class="ms-auto">
						<div class="btn-group">
							<button type="button" class="btn btn-light">Settings</button>
							<button type="button" class="btn btn-light dropdown-toggle dropdown-toggle-split"
								data-bs-toggle="dropdown"> <span class="visually-hidden">Toggle Dropdown</span>
							</button>
							<div class="dropdown-menu dropdown-menu-right dropdown-menu-lg-end"> <a
									class="dropdown-item" href="javascript:;">Action</a>
								<a class="dropdown-item" href="javascript:;">Another action</a>
								<a class="dropdown-item" href="javascript:;">Something else here</a>
								<div class="dropdown-divider"></div> <a class="dropdown-item"
									href="javascript:;">Separated link</a>
							</div>
						</div>
					</div>-->
				</div>
				<!--end breadcrumb-->
				<h6 class="mb-0 text-uppercase">Flugbuch Altes Lager</h6>
				<hr />
				<div class="card">
					<div class="card-body">
						<div class="table-responsive">
							<table id="example" class="table table-striped table-bordered" style="width:100%">
								<thead>
									<tr>
										<th>Datum</th>
										<th>Startzeit</th>
										<th>Landezeit</th>
										<th>Kennzeichen</th>
										<th>Muster</th>
										<th>Pilot</th>
										<th>Besatzung</th>
										<th>Gaeste</th>
										<th>Flugart</th>
										<th>Startplatz</th>
										<th>Zielplatz</th>
										<th>Flugleiter</th>
										<th>Geschleppter</th>
										<th>Anzahl</th>
										<th>Schlepphöhe</th>
										<th>Bemerkung</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									<?php
									foreach ($fluege as $flug) {
										?>
										<tr>
											<td>
												<div class="datagrid-cell datagrid-cell-c2-Datum">
													<?= $flug->datum ?>
												</div>
											</td>
											<td>
												<?= $flug->startzeit ?>
											</td>
											<td>
												<?= $flug->landezeit ?>
											</td>
											<td>
												<?= $flug->kennzeichen ?>
											</td>
											<td>
												<?= $flug->muster ?>
											</td>
											<td>
												<?= $flug->pilot ?>
											</td>
											<td>
												<?= $flug->gaeste ?>
											</td>
											<td>
												<?= $flug->gaeste ?>
											</td>
											<td>
												<?= $flug->flugart ?>
											</td>
											<td>
												<?= $flug->startplatz ?>
											</td>
											<td>
												<?= $flug->zielplatz ?>
											</td>
											<td>
												<?= $flug->flugleiter ?>
											</td>
											<td>
												<?= $flug->geschleppter ?>
											</td>
											<td>
												<?= $flug->anzahl ?>
											</td>
											<td>
												<?= $flug->schlepphoehe ?>
											</td>

											<td>
												<?= $flug->bemerkung ?>
											</td>
											<td>
												<a href="flugbuch_bearbeiten.php?id=<?= $flug->id ?>">bearbeiten</a>
											</td>
										</tr>
										<?php
									}
									?>
								</tbody>
							</table>
						</div>
					</div>
				</div>

			</div>
		</div>
		<!--end page wrapper -->
		<!--start overlay-->
		<div class="overlay toggle-icon"></div>
		<!--end overlay-->
		<!--Start Back To Top Button--> <a href="javaScript:;" class="back-to-top"><i
				class='bx bxs-up-arrow-alt'></i></a>
		<!--End Back To Top Button-->
		<footer class="page-footer">
			<p class="mb-0"></p>
		</footer>
	</div>
	<!--end wrapper-->
	
	<!-- Bootstrap JS -->
	<script src="assets/js/bootstrap.bundle.min.js"></script>
	<!--plugins-->
	<script src="assets/js/jquery.min.js"></script>
	<script src="assets/plugins/simplebar/js/simplebar.min.js"></script>
	<script src="assets/plugins/metismenu/js/metisMenu.min.js"></script>
	<script src="assets/plugins/perfect-scrollbar/js/perfect-scrollbar.js"></script>
	<script src="assets/plugins/datatable/js/jquery.dataTables.min.js"></script>
	<script src="assets/plugins/datatable/js/dataTables.bootstrap5.min.js"></script>


	<!-- <script>
		$(document).ready(function() {
			$('#example').DataTable(
			  
			  );
			$("div.dataTables_filter input").focus();
		  } );
	</script> -->


	<script>

		$(document).ready(function () {
			$('#example').DataTable()
		});

	</script>


	<script>
		$(document).ready(function () {
			var table = $('#example2').DataTable({
				lengthChange: false,
				buttons: ['copy', 'excel', 'pdf', 'print']
			});

			table.buttons().container()
				.appendTo('#example2_wrapper .col-md-6:eq(0)');
		});
	</script>
	<!--notification js -->
	<script src="assets/plugins/notifications/js/lobibox.min.js"></script>
	<script src="assets/plugins/notifications/js/notifications.min.js"></script>
	<script>
		$(document).ready(function () {

			<?php
			if (FlugResult::INFO == $flugResult->resultMessageType) {
				?>
				Lobibox.notify('success', {
					pauseDelayOnHover: true,
					continueDelayOnInactiveTab: false,
					position: 'top right',
					icon: 'bx bx-info-circle',
					title: 'Information',
					msg: '<?= $flugResult->resultMessage ?>'
				});
				<?php
			}
			?>
			<?php
			if (FlugResult::WARNING == $flugResult->resultMessageType) {
				?>
				Lobibox.notify('warning', {
					pauseDelayOnHover: true,
					continueDelayOnInactiveTab: false,
					position: 'top right',
					icon: 'bx bx-info-circle',
					title: 'Achtung',
					msg: '<?= $flugResult->resultMessage ?>'
				});
				<?php
			}
			?>
			<?php
			if (FlugResult::ERROR == $flugResult->resultMessageType) {
				?>
				Lobibox.notify('error', {
					pauseDelayOnHover: true,
					continueDelayOnInactiveTab: false,
					position: 'top right',
					icon: 'bx bx-info-circle',
					title: 'Es ist ein Fehler aufgetreten',
					msg: '<?= $flugResult->resultMessage ?>'
				});
				<?php
			}
			?>

});
	</script>

	<!--app JS-->
	<script src="assets/js/app.js"></script>
</body>

</html>