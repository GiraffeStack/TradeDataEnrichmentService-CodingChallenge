$inputCsv = '.\trade.csv'
$desiredRows = 10000

$data = Import-Csv $inputCsv
$originalRows = $data.Count
$reps = [math]::Ceiling($desiredRows / $originalRows)

1..($reps-1) | ForEach-Object {
    $data += Import-Csv $inputCsv
}

$data | Export-Csv '.\trade_expanded.csv' -NoTypeInformation