<script setup>
import * as echarts from 'echarts';
import {onMounted} from "vue";
import request from "../../utils/request";

onMounted(()=>{
  const chartDom1 = document.getElementById('main1');
  const myChart1 = echarts.init(chartDom1);

  const option1 = {
    title: {
      text: '不同分类商品销售数量',
      subtext: '饼图',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: 'Access From',
        type: 'pie',
        radius: '50%',
        data: [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };

  request.get('/echarts/count1').then(res=>{
    option1.series[0].data=res.data
    option1 && myChart1.setOption(option1);
  })


  const chartDom2 = document.getElementById('main2');
  const myChart2 = echarts.init(chartDom2);

  const option2 = {
    title: {
      text: '最高销售额商品top10',
      subtext: '柱状图',
      left: 'center'
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: [],
        type: 'bar'
      }
    ]
  };

  request.get('/echarts/count2').then(res=>{
    res.data.forEach(item=>{
      option2.xAxis.data.push(item.name)
      option2.series[0].data.push(item.value)
    })
    option2 && myChart2.setOption(option2);
  })
})

</script>

<template>

  <div style="width: 100%;height: 400px;" id="main1">

  </div>

  <div style="width: 100%;height: 400px;margin-top: 20px"  id="main2">

  </div>

</template>

<style scoped>

</style>